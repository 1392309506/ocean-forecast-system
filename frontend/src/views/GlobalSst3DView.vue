<template>
  <section class="page-grid">
    <article class="card globe-card">
      <h2>全球海表温度 3D 可视化</h2>
      <div class="form-inline">
        <label>
          数据源
          <select v-model="dataSource" @change="renderChart">
            <option value="auto">自动（优先后端）</option>
            <option value="api">后端格点数据</option>
            <option value="synthetic">全球模拟场</option>
          </select>
        </label>
        <button @click="renderChart">刷新渲染</button>
      </div>
      <p class="meta">{{ infoText }}</p>
      <div ref="chartRef" class="globe-chart"></div>
    </article>
  </section>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';
import * as echarts from 'echarts';
import 'echarts-gl';
import { fetchGridData } from '../api/oceanApi';

const chartRef = ref(null);
const chart = ref(null);
const dataSource = ref('auto');
const infoText = ref('加载中...');

function tempToColorScale() {
  return [
    [0, '#0b1d4d'],
    [0.2, '#134e9b'],
    [0.4, '#2f9ad8'],
    [0.6, '#f2c572'],
    [0.8, '#cc7a4a'],
    [1, '#8a2f1b']
  ];
}

function createSyntheticGlobalSst() {
  const points = [];
  for (let lat = -85; lat <= 85; lat += 5) {
    for (let lon = -180; lon <= 180; lon += 5) {
      const latRad = (lat * Math.PI) / 180;
      const lonRad = (lon * Math.PI) / 180;
      const temp =
        28 - Math.abs(lat) * 0.25 + Math.sin(lonRad * 2.2) * 2.1 + Math.cos(latRad * 3.1) * 1.6;
      points.push([lon, lat, Number(temp.toFixed(2))]);
    }
  }
  return points;
}

function normalizeApiPoints(grid) {
  if (!grid?.lat?.length || !grid?.lon?.length || !grid?.values?.length) return [];
  const points = [];
  for (let i = 0; i < grid.lat.length; i += 1) {
    for (let j = 0; j < grid.lon.length; j += 1) {
      const v = grid.values?.[i]?.[j];
      if (typeof v === 'number' && Number.isFinite(v)) {
        points.push([grid.lon[j], grid.lat[i], Number(v.toFixed(2))]);
      }
    }
  }
  return points;
}

async function getPlotData() {
  if (dataSource.value === 'synthetic') {
    infoText.value = '当前为全球模拟海温场（演示数据）。';
    return createSyntheticGlobalSst();
  }

  try {
    const res = await fetchGridData({ dataType: 'temperature' });
    const apiPoints = normalizeApiPoints(res?.data);

    if (dataSource.value === 'api') {
      infoText.value = `当前为后端格点数据，共 ${apiPoints.length} 点。`;
      return apiPoints;
    }

    if (apiPoints.length >= 200) {
      infoText.value = `自动模式：使用后端格点数据，共 ${apiPoints.length} 点。`;
      return apiPoints;
    }

    infoText.value =
      '自动模式：后端格点范围较小，已回退到全球模拟海温场（用于 3D 全球可视化）。';
    return createSyntheticGlobalSst();
  } catch (error) {
    infoText.value = '后端不可用，已使用全球模拟海温场。';
    return createSyntheticGlobalSst();
  }
}

async function renderChart() {
  const points = await getPlotData();
  if (!chart.value) {
    chart.value = echarts.init(chartRef.value);
  }

  chart.value.setOption({
    backgroundColor: '#f5f9fc',
    tooltip: {
      formatter: (params) => {
        const [lon, lat, temp] = params.value;
        return `经度 ${lon.toFixed(1)}<br/>纬度 ${lat.toFixed(1)}<br/>温度 ${temp.toFixed(2)} degC`;
      }
    },
    visualMap: {
      min: -2,
      max: 32,
      orient: 'horizontal',
      left: 'center',
      bottom: 16,
      text: ['高温', '低温'],
      calculable: true,
      inRange: {
        color: tempToColorScale().map((it) => it[1])
      }
    },
    globe: {
      baseTexture: '#15293b',
      shading: 'lambert',
      light: {
        main: { intensity: 1.0 },
        ambient: { intensity: 0.45 }
      },
      viewControl: {
        autoRotate: true,
        autoRotateSpeed: 8,
        damping: 0.15,
        alpha: 22,
        beta: 160
      },
      environment: '#d8e5ec'
    },
    series: [
      {
        type: 'scatter3D',
        coordinateSystem: 'globe',
        data: points,
        symbolSize: (value) => Math.max(3, Math.min(11, (value[2] + 2) * 0.26)),
        itemStyle: {
          opacity: 0.92
        }
      }
    ]
  });
}

function handleResize() {
  if (chart.value) chart.value.resize();
}

onMounted(() => {
  renderChart();
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  if (chart.value) {
    chart.value.dispose();
    chart.value = null;
  }
});
</script>
