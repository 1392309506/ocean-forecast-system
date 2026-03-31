<template>
  <section class="page-grid">
    <article class="card">
      <h2>预报解译</h2>
      <div class="form-inline">
        <label>海区 <input v-model="region" /></label>
        <label>时效(h) <input type="number" v-model.number="forecastHours" min="6" step="6" /></label>
        <button @click="loadForecast">刷新</button>
      </div>

      <div class="badge-row" v-if="forecast">
        <span class="badge">准确率 {{ (forecast.accuracy * 100).toFixed(1) }}%</span>
        <span class="badge">置信区间 {{ forecast.confidenceLower }} ~ {{ forecast.confidenceUpper }}</span>
      </div>

      <p class="meta" v-if="errorMessage">{{ errorMessage }}</p>

      <ul class="timeline" v-if="forecast">
        <li v-for="(time, idx) in forecast.forecastTimes" :key="time">
          <strong>T+{{ idx * 6 }}h</strong> {{ time }}
        </li>
      </ul>
    </article>
  </section>
</template>

<script setup>
import { ref } from 'vue';
import { fetchForecastData } from '../api/oceanApi';

const region = ref('东海');
const forecastHours = ref(72);
const forecast = ref(null);
const errorMessage = ref('');

async function loadForecast() {
  try {
    errorMessage.value = '';
    const res = await fetchForecastData({
      region: region.value,
      forecastHours: forecastHours.value
    });
    forecast.value = res.data;
  } catch (error) {
    forecast.value = null;
    errorMessage.value = '预报数据加载失败，请检查后端服务与数据库表是否完整';
  }
}

loadForecast();
</script>
