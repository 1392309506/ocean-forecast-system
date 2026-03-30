import { createRouter, createWebHistory } from 'vue-router';
import DashboardView from '../views/DashboardView.vue';
import MapView from '../views/MapView.vue';
import TimeSeriesView from '../views/TimeSeriesView.vue';
import ForecastView from '../views/ForecastView.vue';

const routes = [
  { path: '/', name: 'dashboard', component: DashboardView },
  { path: '/map', name: 'map', component: MapView },
  { path: '/timeseries', name: 'timeseries', component: TimeSeriesView },
  { path: '/forecast', name: 'forecast', component: ForecastView }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
