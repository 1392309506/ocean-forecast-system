<template>
  <div class="app-shell">
    <header class="topbar">
      <div>
        <h1>Ocean Forecast Research Console</h1>
        <p>海洋预报科研可视分析平台</p>
      </div>
      <nav class="nav-tabs">
        <RouterLink to="/">概览</RouterLink>
        <RouterLink to="/map">空间分布</RouterLink>
        <RouterLink to="/globe-sst">全球海温3D</RouterLink>
        <RouterLink to="/timeseries">时间序列</RouterLink>
        <RouterLink to="/forecast">预报解译</RouterLink>
      </nav>
    </header>

    <main class="main-panel">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { RouterLink, RouterView } from 'vue-router';
</script>
