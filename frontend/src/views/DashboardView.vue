<template>
  <section class="page-grid">
    <article class="card hero">
      <h2>系统概览</h2>
      <p>
        平台聚焦于海温、盐度、流速与波高等关键指标，面向科研分析场景提供可复用的观测与预报接口。
      </p>
      <div class="status-row">
        <span class="dot" :class="healthOk ? 'ok' : 'bad'"></span>
        <span>服务状态：{{ healthText }}</span>
      </div>
    </article>

    <article class="card metric" v-for="item in metrics" :key="item.label">
      <h3>{{ item.label }}</h3>
      <p class="metric-value">{{ item.value }}</p>
      <small>{{ item.note }}</small>
    </article>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { fetchHealth } from '../api/oceanApi';

const healthText = ref('检查中');
const healthOk = computed(() => healthText.value.toLowerCase().includes('running'));

const metrics = [
  { label: '核心接口', value: '5', note: 'grid / timeseries / forecast / observation / health' },
  { label: '预报步长', value: '6h', note: '支持 72h 滚动预报解译' },
  { label: '研究海域', value: '东海', note: '可扩展至多海区业务域' }
];

onMounted(async () => {
  try {
    const resp = await fetchHealth();
    healthText.value = resp?.data || '服务异常';
  } catch (error) {
    healthText.value = '服务不可达';
  }
});
</script>
