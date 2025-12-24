<script setup>
import { RouterLink } from 'vue-router';
</script>

<template>
  <nav class="navbar navbar-expand-lg navbar-dark bg-dark mb-4">
    <div class="container-fluid">
      <RouterLink class="navbar-brand" to="/">TimeTracker</RouterLink>

      <button
        class="navbar-toggler"
        type="button"
        data-bs-toggle="collapse"
        data-bs-target="#mainNavbar"
        aria-controls="mainNavbar"
        aria-expanded="false"
        aria-label="Toggle navigation"
      >
        <span class="navbar-toggler-icon"></span>
      </button>

      <div class="collapse navbar-collapse" id="mainNavbar">
        <ul class="navbar-nav">
          <li class="nav-item">
            <RouterLink class="nav-link" to="/" active-class="active" end>
              Dashboard
            </RouterLink>
          </li>

          <li class="nav-item">
            <RouterLink class="nav-link" to="/report" active-class="active">
              Report
            </RouterLink>
          </li>

          <li class="nav-item">
            <RouterLink class="nav-link" to="/activities" active-class="active">
              Activities
            </RouterLink>
          </li>

          <li class="nav-item">
            <RouterLink class="nav-link" to="/categories" active-class="active">
              Categories
            </RouterLink>
          </li>

          <li class="nav-item">
            <a class="nav-link" target="_blank" href="/swagger-ui/index.html">API Docs</a>
          </li>
        </ul>
      </div>
    </div>
  </nav>
</template>
