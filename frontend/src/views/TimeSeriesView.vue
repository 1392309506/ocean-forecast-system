<template>
  <section class="page-grid">
    <article class="card">
      <h2>时间序列分析</h2>
      <div class="form-inline">
        <label>经度 <input type="number" step="0.1" v-model.number="longitude" /></label>
        <label>纬度 <input type="number" step="0.1" v-model.number="latitude" /></label>
        <button @click="loadSeries">查询</button>
      </div>

      <svg class="sparkline" viewBox="0 0 600 120" preserveAspectRatio="none" v-if="points.length">
        <polyline :points="points" fill="none" stroke="#1e6a83" stroke-width="3" />
      </svg>

      <p class="meta">{{ statText }}</p>
    </article>
  </section>
</template>

<script setup>
import { computed, ref } from 'vue';
import { fetchTimeSeriesData } from '../api/oceanApi';

const longitude = ref(120.5);
const latitude = ref(30.5);
const values = ref([]);

const points = computed(() => {
  if (!values.value.length) return '';
  const max = Math.max(...values.value);
  const min = Math.min(...values.value);
  const range = max - min || 1;
  return values.value
    .map((v, i) => {
      const x = (i / (values.value.length - 1)) * 600;
      const y = 110 - ((v - min) / range) * 100;
      return `${x},${y}`;
    })
    .join(' ');
});

const statText = computed(() => {
  if (!values.value.length) return '暂无序列数据';
  const avg = values.value.reduce((acc, n) => acc + n, 0) / values.value.length;
  return `样本数 ${values.value.length} | 均值 ${avg.toFixed(2)} | 最小 ${Math.min(...values.value).toFixed(2)} | 最大 ${Math.max(...values.value).toFixed(2)}`;
});

async function loadSeries() {
  const res = await fetchTimeSeriesData({
    longitude: longitude.value,
    latitude: latitude.value,
    dataType: 'temperature'
  });
  values.value = res.data.values || [];
}

loadSeries();
</script>
