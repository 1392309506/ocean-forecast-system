import http from './http';

export async function fetchHealth() {
  const res = await http.get('/ocean/health');
  return res.data;
}

export async function fetchGridData(params) {
  const res = await http.get('/ocean/grid', { params });
  return res.data;
}

export async function fetchTimeSeriesData(params) {
  const res = await http.get('/ocean/timeseries', { params });
  return res.data;
}

export async function fetchForecastData(params) {
  const res = await http.get('/ocean/forecast', { params });
  return res.data;
}

export async function fetchObservationData(params) {
  const res = await http.get('/ocean/observation', { params });
  return res.data;
}
