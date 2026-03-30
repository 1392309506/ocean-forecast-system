<template>
  <section class="page-grid">
    <article class="card">
      <h2>空间分布分析</h2>
      <div class="form-inline">
        <label>
          数据类型
          <select v-model="dataType">
            <option value="temperature">temperature</option>
            <option value="salinity">salinity</option>
            <option value="current_speed">current_speed</option>
            <option value="wave_height">wave_height</option>
          </select>
        </label>
        <button @click="loadData">刷新</button>
      </div>
      <p class="meta">{{ summary }}</p>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>纬度</th>
              <th>经度</th>
              <th>值</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in sampledRows" :key="row.key">
              <td>{{ row.lat }}</td>
              <td>{{ row.lon }}</td>
              <td>{{ row.value }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </article>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { fetchGridData } from '../api/oceanApi';

const dataType = ref('temperature');
const grid = ref(null);

const summary = computed(() => {
  if (!grid.value) return '尚未加载数据';
  return `最小值 ${grid.value.minValue?.toFixed(2)} ${grid.value.unit} | 最大值 ${grid.value.maxValue?.toFixed(2)} ${grid.value.unit}`;
});

const sampledRows = computed(() => {
  if (!grid.value) return [];
  const rows = [];
  for (let i = 0; i < Math.min(10, grid.value.lat.length); i += 2) {
    const j = i;
    rows.push({
      key: `${i}-${j}`,
      lat: grid.value.lat[i].toFixed(2),
      lon: grid.value.lon[j].toFixed(2),
      value: grid.value.values[i][j].toFixed(2)
    });
  }
  return rows;
});

async function loadData() {
  const res = await fetchGridData({ dataType: dataType.value });
  grid.value = res.data;
}

onMounted(loadData);
</script>
