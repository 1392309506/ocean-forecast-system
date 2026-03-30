package com.ocean.forecast.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocean.forecast.entity.GridData;
import org.junit.jupiter.api.Test;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Index;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetcdfVisualizationDemoTest {

    private static final String DEMO_NETCDF = "src/main/resources/glo12v1_monthly_2013_05.nc";

    @Test
    void shouldReadNetcdfAndGenerateGridDemoJson() throws IOException, InvalidRangeException {
        Path ncPath = Paths.get(DEMO_NETCDF);
        assertTrue(Files.exists(ncPath), "Demo NetCDF file not found: " + ncPath.toAbsolutePath());

        try (NetcdfFile netcdf = NetcdfFiles.open(ncPath.toAbsolutePath().toString())) {
            Variable lonVar = findCoordinateVariable(netcdf, "lon", "longitude", "x");
            Variable latVar = findCoordinateVariable(netcdf, "lat", "latitude", "y");
            assertNotNull(lonVar, "Cannot find longitude variable in NetCDF file");
            assertNotNull(latVar, "Cannot find latitude variable in NetCDF file");

            double[] lon = toDouble1D(lonVar.read());
            double[] lat = toDouble1D(latVar.read());

            Variable dataVar = findMainDataVariable(netcdf, lonVar, latVar);
            assertNotNull(dataVar, "Cannot find a numeric data variable that uses lat/lon dimensions");

            double[][] values = readGridValues(dataVar, lon.length, lat.length, lonVar.getShortName(),
                    latVar.getShortName());
            MinMax minMax = calculateMinMax(values);

            GridData demo = GridData.builder()
                    .lon(lon)
                    .lat(lat)
                    .values(values)
                    .dataType(dataVar.getShortName())
                    .unit(readUnit(dataVar))
                    .minValue(minMax.min)
                    .maxValue(minMax.max)
                    .timestamp("2013-05-monthly")
                    .build();

            assertTrue(lon.length > 0, "Longitude array should not be empty");
            assertTrue(lat.length > 0, "Latitude array should not be empty");
            assertTrue(values.length > 0, "Grid values should not be empty");
            assertTrue(minMax.max >= minMax.min, "maxValue should be >= minValue");

            Path output = Paths.get("target", "demo", "monthly-grid-demo.json");
            Files.createDirectories(output.getParent());
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(output.toFile(), demo);
            assertTrue(Files.exists(output), "Demo JSON was not generated");
        }
    }

    private Variable findCoordinateVariable(NetcdfFile netcdf, String... candidates) {
        for (String candidate : candidates) {
            Variable v = netcdf.findVariable(candidate);
            if (v != null) {
                return v;
            }
        }
        for (Variable variable : netcdf.getVariables()) {
            String name = variable.getShortName().toLowerCase(Locale.ROOT);
            for (String candidate : candidates) {
                if (name.equals(candidate) || name.contains(candidate)) {
                    return variable;
                }
            }
        }
        return null;
    }

    private Variable findMainDataVariable(NetcdfFile netcdf, Variable lonVar, Variable latVar) {
        String lonDim = firstDimensionName(lonVar);
        String latDim = firstDimensionName(latVar);

        List<Variable> candidates = new ArrayList<>();
        for (Variable variable : netcdf.getVariables()) {
            if (!variable.getDataType().isNumeric() || variable.getRank() < 2) {
                continue;
            }
            List<String> dimNames = variable.getDimensions().stream()
                    .map(Dimension::getShortName)
                    .map(s -> s.toLowerCase(Locale.ROOT))
                    .toList();
            boolean usesLon = dimNames.contains(lonDim.toLowerCase(Locale.ROOT));
            boolean usesLat = dimNames.contains(latDim.toLowerCase(Locale.ROOT));
            if (usesLon && usesLat) {
                candidates.add(variable);
            }
        }

        Optional<Variable> preferred = candidates.stream()
                .filter(v -> {
                    String n = v.getShortName().toLowerCase(Locale.ROOT);
                    return n.contains("temp") || n.contains("theta") || n.contains("sst") || n.contains("salinity")
                            || n.contains("so");
                })
                .findFirst();

        if (preferred.isPresent()) {
            return preferred.get();
        }

        return candidates.stream()
                .max(Comparator.comparingInt(Variable::getSize))
                .orElse(null);
    }

    private String firstDimensionName(Variable variable) {
        return variable.getDimensions().isEmpty()
                ? variable.getShortName()
                : variable.getDimension(0).getShortName();
    }

    private double[] toDouble1D(Array array) {
        int size = (int) array.getSize();
        double[] result = new double[size];
        Index index = array.getIndex();
        for (int i = 0; i < size; i++) {
            result[i] = array.getDouble(index.set(i));
        }
        return result;
    }

    private double[][] readGridValues(Variable dataVar, int lonSize, int latSize, String lonName, String latName)
            throws IOException, InvalidRangeException {
        int rank = dataVar.getRank();
        int[] shape = dataVar.getShape();
        int[] origin = new int[rank];
        int[] readShape = shape.clone();

        for (int i = 0; i < rank - 2; i++) {
            readShape[i] = 1;
        }

        Array data = dataVar.read(origin, readShape).reduce();
        assertTrue(data.getRank() == 2, "Reduced data variable must be 2D for visualization");

        List<String> dimNames = dataVar.getDimensions().stream()
                .map(Dimension::getShortName)
                .map(s -> s.toLowerCase(Locale.ROOT))
                .toList();

        int lonIndexInOriginal = findDimensionIndex(dimNames, lonName);
        int latIndexInOriginal = findDimensionIndex(dimNames, latName);
        assertTrue(lonIndexInOriginal >= 0 && latIndexInOriginal >= 0, "Data variable missing lon/lat dimensions");

        int lonIndex2d = convertToReducedAxis(rank, lonIndexInOriginal);
        int latIndex2d = convertToReducedAxis(rank, latIndexInOriginal);

        int[] reducedShape = data.getShape();
        assertTrue(reducedShape[lonIndex2d] == lonSize || reducedShape[latIndex2d] == latSize,
                "Grid shape does not match lon/lat coordinate lengths");

        double[][] values = new double[latSize][lonSize];
        Index index = data.getIndex();

        for (int lat = 0; lat < latSize; lat++) {
            for (int lon = 0; lon < lonSize; lon++) {
                int[] idx = new int[2];
                idx[latIndex2d] = lat;
                idx[lonIndex2d] = lon;
                double v = data.getDouble(index.set(idx));
                values[lat][lon] = v;
            }
        }
        return values;
    }

    private int findDimensionIndex(List<String> dimNames, String targetName) {
        String target = targetName.toLowerCase(Locale.ROOT);
        for (int i = 0; i < dimNames.size(); i++) {
            String dim = dimNames.get(i);
            if (dim.equals(target) || dim.contains(target)) {
                return i;
            }
        }
        if (target.contains("lon")) {
            for (int i = 0; i < dimNames.size(); i++) {
                if (dimNames.get(i).contains("x") || dimNames.get(i).contains("lon")) {
                    return i;
                }
            }
        }
        if (target.contains("lat")) {
            for (int i = 0; i < dimNames.size(); i++) {
                if (dimNames.get(i).contains("y") || dimNames.get(i).contains("lat")) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int convertToReducedAxis(int originalRank, int originalAxis) {
        if (originalRank <= 2) {
            return originalAxis;
        }
        return originalAxis - (originalRank - 2);
    }

    private String readUnit(Variable variable) {
        String units = variable.findAttributeString("units", "");
        if (units == null || units.isBlank()) {
            return "unknown";
        }
        return units;
    }

    private MinMax calculateMinMax(double[][] values) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (double[] row : values) {
            for (double value : row) {
                if (Double.isNaN(value) || Double.isInfinite(value)) {
                    continue;
                }
                if (value < min) {
                    min = value;
                }
                if (value > max) {
                    max = value;
                }
            }
        }
        assertFalse(min == Double.POSITIVE_INFINITY, "No valid numeric values found in dataset");
        return new MinMax(min, max);
    }

    private record MinMax(double min, double max) {
    }
}
