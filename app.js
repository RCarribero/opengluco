/**
 * OpenGluco v1.2.2 — Interactive Clinical Web Application
 * Faithful 1:1 implementation of app-mobile, app-wear, and app-auto
 */
(function () {
  'use strict';

  const PATIENTS = [
    {
      id: 'demo_patient_1',
      firstName: 'Rubén',
      lastName: 'Carribero',
      role: 'Paciente Titular (Tipo 1)',
      targetLow: 70,
      targetHigh: 180,
      sensor: {
        deviceId: 'DEMO-SENSOR-01',
        serialNumber: 'MH01DEMO2026',
        modelName: 'FreeStyle Libre 3 Plus',
        lifetimeDays: 15,
        remainingDays: 12,
        isActive: true
      },
      currentMgDl: 114,
      trendArrow: 3, // 1: ↓, 2: ↘, 3: →, 4: ↗, 5: ↑
      historyMgDl: [104, 107, 110, 116, 128, 142, 148, 139, 126, 119, 112, 106, 103, 109, 121, 134, 129, 118, 114]
    },
    {
      id: 'demo_patient_2',
      firstName: 'Elena',
      lastName: 'Martínez',
      role: 'Supervisión Clínica ( Monitor Remoto )',
      targetLow: 75,
      targetHigh: 170,
      sensor: {
        deviceId: 'DEMO-SENSOR-02',
        serialNumber: 'FS02CLINIC99',
        modelName: 'FreeStyle Libre 2',
        lifetimeDays: 14,
        remainingDays: 9,
        isActive: true
      },
      currentMgDl: 132,
      trendArrow: 4,
      historyMgDl: [96, 101, 108, 118, 130, 145, 158, 162, 151, 138, 124, 115, 112, 117, 123, 127, 129, 131, 132]
    }
  ];

  const state = {
    isAuthenticated: true,
    activeDevice: 'mobile', // 'mobile' | 'wear' | 'auto'
    mobileSubScreen: 'dashboard', // 'dashboard' | 'settings' | 'reports' | 'qr'
    selectedPatientIdx: 0,
    unit: 'mg/dL', // 'mg/dL' | 'mmol/L'
    isDark: true,
    timeframeHours: 6, // 1, 3, 6, 12, 24
    statsPeriodDays: 7, // 1, 7, 30, 90
    alarmTriggered: null,
    alarms: [
      { id: 'urgent_low', name: 'Hipoglucemia Urgente', threshold: 55, condition: 'below', enabled: true, repeatMin: 1, sound: 'Urgente Extremo', escalate: true },
      { id: 'low', name: 'Hipoglucemia', threshold: 70, condition: 'below', enabled: true, repeatMin: 2, sound: 'Alerta Médica', escalate: true },
      { id: 'high', name: 'Hiperglucemia', threshold: 180, condition: 'above', enabled: true, repeatMin: 4, sound: 'Discreto Clínico', escalate: false }
    ],
    scrubIndex: null
  };

  function getPatient() {
    return PATIENTS[state.selectedPatientIdx];
  }

  function formatGlucose(mgdl) {
    if (state.unit === 'mmol/L') {
      return (mgdl / 18.0182).toFixed(1);
    }
    return Math.round(mgdl).toString();
  }

  function getTrendMeta(arrowCode) {
    switch (arrowCode) {
      case 1: return { symbol: '↓', text: 'Bajando rápido', delta: '-3.2 mg/dL/min' };
      case 2: return { symbol: '↘', text: 'Bajando', delta: '-1.4 mg/dL/min' };
      case 4: return { symbol: '↗', text: 'Subiendo', delta: '+1.5 mg/dL/min' };
      case 5: return { symbol: '↑', text: 'Subiendo rápido', delta: '+3.1 mg/dL/min' };
      default: return { symbol: '→', text: 'Estable', delta: '+0.1 mg/dL/min' };
    }
  }

  function getClinicalStatus(mgdl, targetLow, targetHigh) {
    if (mgdl <= 55) {
      return { color: '#EF4444', bg: 'rgba(239, 68, 68, 0.18)', label: 'Urgente Bajo', level: 'URGENT_LOW' };
    }
    if (mgdl < targetLow) {
      return { color: '#F87171', bg: 'rgba(248, 113, 113, 0.18)', label: 'Bajo (Hipoglucemia)', level: 'LOW' };
    }
    if (mgdl >= 250) {
      return { color: '#FB923C', bg: 'rgba(251, 146, 60, 0.18)', label: 'Muy Alto', level: 'VERY_HIGH' };
    }
    if (mgdl > targetHigh) {
      return { color: '#FBBF24', bg: 'rgba(251, 191, 36, 0.18)', label: 'Alto (Hiperglucemia)', level: 'HIGH' };
    }
    return { color: '#4ADE80', bg: 'rgba(74, 222, 128, 0.16)', label: 'En Rango Clínico', level: 'IN_RANGE' };
  }

  function updateCssVariables() {
    const p = getPatient();
    const st = getClinicalStatus(p.currentMgDl, p.targetLow, p.targetHigh);
    document.documentElement.style.setProperty('--active-status', st.color);
    document.documentElement.style.setProperty('--active-status-bg', st.bg);

    const slider = document.getElementById('og-glucose-slider');
    const readout = document.getElementById('og-slider-readout');
    if (slider) slider.value = p.currentMgDl;
    if (readout) {
      readout.textContent = formatGlucose(p.currentMgDl) + ' ' + state.unit;
      readout.style.color = st.color;
    }
  }

  function playClinicalTone(isUrgent) {
    try {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      if (!AudioCtx) return;
      const ctx = new AudioCtx();
      const freqs = isUrgent ? [880, 1174, 880, 1174] : [659, 784];
      freqs.forEach((f, idx) => {
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.type = isUrgent ? 'sawtooth' : 'sine';
        osc.frequency.value = f;
        gain.gain.setValueAtTime(0.08, ctx.currentTime + idx * 0.16);
        gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + (idx + 1) * 0.15);
        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.start(ctx.currentTime + idx * 0.16);
        osc.stop(ctx.currentTime + (idx + 1) * 0.16);
      });
    } catch (_) {}
  }

  function evaluateAlarms(mgdl) {
    const urgent = state.alarms.find(a => a.id === 'urgent_low' && a.enabled);
    const low = state.alarms.find(a => a.id === 'low' && a.enabled);
    const high = state.alarms.find(a => a.id === 'high' && a.enabled);

    if (urgent && mgdl <= urgent.threshold) {
      state.alarmTriggered = { alarm: urgent, value: mgdl };
      playClinicalTone(true);
    } else if (low && mgdl < low.threshold) {
      state.alarmTriggered = { alarm: low, value: mgdl };
      playClinicalTone(false);
    } else if (high && mgdl > high.threshold) {
      state.alarmTriggered = { alarm: high, value: mgdl };
      playClinicalTone(false);
    } else {
      state.alarmTriggered = null;
    }
  }

  function setLiveGlucose(mgdl, trendArrow) {
    const p = getPatient();
    p.currentMgDl = Math.max(40, Math.min(300, Math.round(mgdl)));
    if (trendArrow) p.trendArrow = trendArrow;
    p.historyMgDl.push(p.currentMgDl);
    if (p.historyMgDl.length > 24) p.historyMgDl.shift();
    evaluateAlarms(p.currentMgDl);
    render();
  }

  /* ==========================================================================
     RENDERING VIEWS
     ========================================================================== */
  function render() {
    updateCssVariables();
    const simStrip = document.getElementById('og-sim-strip');
    if (simStrip) {
      simStrip.style.display = state.isAuthenticated ? 'flex' : 'none';
    }

    const root = document.getElementById('og-app-root');
    if (!root) return;

    if (!state.isAuthenticated) {
      root.innerHTML = renderLoginScreen();
      bindLoginEvents();
      return;
    }

    if (state.activeDevice === 'wear') {
      root.innerHTML = renderWearOsView();
      bindWearEvents();
      drawWearSparkline();
    } else if (state.activeDevice === 'auto') {
      root.innerHTML = renderAndroidAutoView();
      bindAutoEvents();
    } else {
      if (state.mobileSubScreen === 'settings') {
        root.innerHTML = renderMobileSettings();
        bindSettingsEvents();
      } else if (state.mobileSubScreen === 'reports') {
        root.innerHTML = renderMobileReports();
        bindReportsEvents();
      } else if (state.mobileSubScreen === 'qr') {
        root.innerHTML = renderMobileQr();
        bindQrEvents();
      } else {
        root.innerHTML = renderMobileDashboard();
        bindDashboardEvents();
        drawBezierChart();
      }
    }
  }

  function renderLoginScreen() {
    return `
      <div class="og-login-card">
        <div class="og-login-orb">
          <img src="logo.png" alt="OpenGluco" />
        </div>
        <h1 style="font-size:26px; font-weight:800; margin-bottom:4px;">OpenGluco</h1>
        <p style="font-size:13px; color:var(--text-secondary); margin-bottom:24px;">
          Monitor Clínico de Glucosa en Tiempo Real
        </p>

        <form id="og-login-form">
          <div class="og-field-group">
            <label class="og-field-label">Correo electrónico (LibreLinkUp)</label>
            <input type="email" class="og-input" value="demo@opengluco.org" required />
          </div>
          <div class="og-field-group" style="margin-bottom:20px;">
            <label class="og-field-label">Contraseña</label>
            <input type="password" class="og-input" value="••••••••••••" required />
          </div>
          <button type="submit" class="og-btn-primary">Iniciar Sesión</button>
        </form>

        <button type="button" id="og-demo-patient-1" class="og-btn-outline">
          Acceder al Modo Demo Clínico (Rubén Carribero)
        </button>
        <button type="button" id="og-demo-patient-2" class="og-btn-outline" style="border-color:var(--color-cyan); color:var(--color-cyan); background:rgba(56,189,248,0.06);">
          Acceder en Modo Supervisión (Elena Martínez)
        </button>

        <p style="margin-top:20px; font-size:11.5px; color:var(--text-muted); line-height:1.5;">
          Cifrado local AES-256-GCM respaldado por Android Keystore. Cumplimiento estricto GDPR Art. 17 y Art. 20.
        </p>
      </div>
    `;
  }

  function renderMobileDashboard() {
    const p = getPatient();
    const st = getClinicalStatus(p.currentMgDl, p.targetLow, p.targetHigh);
    const trend = getTrendMeta(p.trendArrow);

    const valid = p.historyMgDl;
    const avg = valid.reduce((a, b) => a + b, 0) / valid.length;
    const min = Math.min(...valid);
    const max = Math.max(...valid);
    const inRange = valid.filter(v => v >= p.targetLow && v <= p.targetHigh).length;
    const tir = Math.round((inRange / valid.length) * 100);
    const gmi = (3.31 + 0.02392 * avg).toFixed(1);
    const sensorPct = Math.round((p.sensor.remainingDays / p.sensor.lifetimeDays) * 100);

    return `
      <div class="og-mobile-dashboard">
        <!-- TopAppBar -->
        <div class="og-top-appbar">
          <button type="button" class="og-patient-chip" id="og-open-patient-modal" title="Cambiar paciente activo">
            <span class="og-patient-avatar">${p.firstName[0]}${p.lastName[0]}</span>
            <div style="text-align:left;">
              <div style="font-size:13.5px; font-weight:700; line-height:1.1;">${p.firstName} ${p.lastName}</div>
              <div style="font-size:11px; color:var(--text-secondary);">${p.role}</div>
            </div>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-left:4px; color:var(--text-secondary);">
              <polyline points="6 9 12 15 18 9"></polyline>
            </svg>
          </button>

          <div class="og-appbar-actions">
            <button type="button" class="og-icon-btn" id="og-btn-refresh" title="Sincronizar telemetría ahora">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="23 4 23 10 17 10"></polyline>
                <polyline points="1 20 1 14 7 14"></polyline>
                <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
              </svg>
            </button>
            <button type="button" class="og-icon-btn" id="og-btn-reports" title="Informes Clínicos AGP y Exportación CSV">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                <polyline points="14 2 14 8 20 8"></polyline>
                <line x1="16" y1="13" x2="8" y2="13"></line>
                <line x1="16" y1="17" x2="8" y2="17"></line>
              </svg>
            </button>
            <button type="button" class="og-icon-btn" id="og-btn-settings" title="Ajustes Clínicos y Alarmas">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="3"></circle>
                <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
              </svg>
            </button>
            <button type="button" class="og-icon-btn" id="og-btn-logout" title="Volver a pantalla de Login">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                <polyline points="16 17 21 12 16 7"></polyline>
                <line x1="21" y1="12" x2="9" y2="12"></line>
              </svg>
            </button>
          </div>
        </div>

        ${state.alarmTriggered ? `
          <div class="og-alarm-banner">
            <div>
              <div style="font-size:13px; font-weight:800; color:#EF4444; text-transform:uppercase; letter-spacing:0.5px;">
                Alerta Clínica Activa: ${state.alarmTriggered.alarm.name}
              </div>
              <div style="font-size:12.5px; color:var(--text-primary); margin-top:2px;">
                Lectura actual: <strong>${formatGlucose(state.alarmTriggered.value)} ${state.unit} (${trend.symbol})</strong> — Sincronizado con reloj Wear OS
              </div>
            </div>
            <button type="button" id="og-dismiss-alarm" class="og-preset-btn" style="background:#EF4444; color:#FFF; border:none; padding:8px 14px;">
              Silenciar Alarma
            </button>
          </div>
        ` : ''}

        <!-- Dual Column Responsive Layout -->
        <div class="og-dashboard-grid">
          <!-- Left Column: Hero Orbs + Sensor Card -->
          <div class="og-col">
            <div class="og-card">
              <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
                <span style="font-size:11.5px; font-weight:700; color:var(--text-secondary); text-transform:uppercase; letter-spacing:0.6px;">
                  Estado Glucémico Actual
                </span>
                <span style="font-size:11px; font-weight:700; color:${st.color}; padding:2px 8px; border-radius:99px; background:${st.bg};">
                  ${st.label}
                </span>
              </div>

              <!-- MobileDualFloatingOrbs -->
              <div class="og-orbs-row">
                <!-- Left Orb: Glucose Value -->
                <div class="og-orb" id="og-orb-glucose" title="Pulsar para ver desglose clínico">
                  <svg class="og-orb-svg" viewBox="0 0 138 138">
                    <path
                      d="M 28.5 109.5 A 57 57 0 1 1 109.5 109.5"
                      fill="none"
                      stroke="${st.color}"
                      stroke-width="4.5"
                      stroke-linecap="round"
                    />
                  </svg>
                  <div class="og-orb-value">${formatGlucose(p.currentMgDl)}</div>
                  <div class="og-orb-unit">${state.unit}</div>
                </div>

                <!-- Right Orb: Clinical Trend -->
                <div class="og-orb" id="og-orb-trend" title="Pulsar para ver cinética de tendencia">
                  <div class="og-orb-arrow">${trend.symbol}</div>
                  <div class="og-orb-badge" style="color:${st.color}; background:${st.bg};">
                    ${trend.text}
                  </div>
                </div>
              </div>

              <div style="text-align:center; font-size:11.5px; color:var(--text-muted); margin-top:6px;">
                Actualizado hace unos segundos · Tasa: ${trend.delta}
              </div>
            </div>

            <!-- Dedicated Sensor Info Card -->
            <div class="og-card" id="og-sensor-card" style="cursor:pointer;" title="Pulsar para configurar duración del sensor (14d / 15d)">
              <div style="display:flex; justify-content:space-between; align-items:center;">
                <div>
                  <div style="font-size:11px; font-weight:700; color:var(--text-secondary); text-transform:uppercase;">
                    Sensor Activo (${p.sensor.lifetimeDays} días)
                  </div>
                  <div style="font-size:15px; font-weight:700; margin-top:2px;">
                    ${p.sensor.modelName}
                  </div>
                </div>
                <span style="font-family:'JetBrains Mono',monospace; font-size:13px; font-weight:700; color:var(--color-mint); background:rgba(74,222,128,0.14); padding:4px 10px; border-radius:10px;">
                  ${p.sensor.remainingDays}d restantes
                </span>
              </div>

              <div class="og-progress-track">
                <div class="og-progress-fill" style="width:${sensorPct}%;"></div>
              </div>

              <div style="display:flex; justify-content:space-between; font-size:11.5px; color:var(--text-secondary);">
                <span>S/N: <strong style="font-family:'JetBrains Mono',monospace;">${p.sensor.serialNumber}</strong></span>
                <span style="color:var(--color-mint); font-weight:600;">Cambiar 14d / 15d →</span>
              </div>
            </div>
          </div>

          <!-- Right Column: Continuous Bezier Chart + Clinical Stats -->
          <div class="og-col">
            <div class="og-card">
              <div class="og-chart-header">
                <div>
                  <div style="font-size:14px; font-weight:700;">Curva Continua de Bézier</div>
                  <div style="font-size:11.5px; color:var(--text-secondary);" id="og-chart-sub">
                    Rango objetivo: ${formatGlucose(p.targetLow)} – ${formatGlucose(p.targetHigh)} ${state.unit} (Desliza sobre la gráfica)
                  </div>
                </div>
                <div class="og-timeframe-pills">
                  ${[1, 3, 6, 12, 24].map(h => `
                    <button type="button" class="og-tf-btn ${state.timeframeHours === h ? 'active' : ''}" data-tf="${h}">
                      ${h}H
                    </button>
                  `).join('')}
                </div>
              </div>

              <div class="og-chart-canvas-wrap">
                <canvas id="og-bezier-canvas" width="600" height="210" style="width:100%; height:210px; display:block;"></canvas>
              </div>
            </div>

            <!-- Clinical Statistics Card -->
            <div class="og-card">
              <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:8px;">
                <div>
                  <div style="font-size:14px; font-weight:700;">Métricas Estadísticas Clínicas</div>
                  <div style="font-size:11.5px; color:var(--text-secondary);">GMI estimado: <strong>${gmi}%</strong> · Datos verificados</div>
                </div>
                <div class="og-timeframe-pills">
                  ${[
                    { d: 1, l: '24H' },
                    { d: 7, l: '7D' },
                    { d: 30, l: '30D' },
                    { d: 90, l: '90D' }
                  ].map(pItem => `
                    <button type="button" class="og-tf-btn ${state.statsPeriodDays === pItem.d ? 'active' : ''}" data-period="${pItem.d}">
                      ${pItem.l}
                    </button>
                  `).join('')}
                </div>
              </div>

              <div class="og-stats-grid">
                <div class="og-stat-box" data-stat="tir">
                  <div class="og-stat-lbl">Tiempo en Rango</div>
                  <div class="og-stat-val" style="color:var(--color-mint);">${tir}%</div>
                </div>
                <div class="og-stat-box" data-stat="avg">
                  <div class="og-stat-lbl">Promedio</div>
                  <div class="og-stat-val">${formatGlucose(avg)}</div>
                </div>
                <div class="og-stat-box" data-stat="min">
                  <div class="og-stat-lbl">Mínimo</div>
                  <div class="og-stat-val" style="color:var(--color-low);">${formatGlucose(min)}</div>
                </div>
                <div class="og-stat-box" data-stat="max">
                  <div class="og-stat-lbl">Máximo</div>
                  <div class="og-stat-val" style="color:var(--color-high);">${formatGlucose(max)}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  /* ==========================================================================
     SETTINGS, ALARMS, REPORTS & QR SCREENS
     ========================================================================== */
  function renderMobileSettings() {
    const p = getPatient();
    return `
      <div class="og-mobile-dashboard" style="max-width:680px;">
        <div class="og-top-appbar">
          <button type="button" class="og-patient-chip" id="og-back-dashboard">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="19" y1="12" x2="5" y2="12"></line>
              <polyline points="12 19 5 12 12 5"></polyline>
            </svg>
            <span style="font-weight:700; font-size:14px;">Volver al Panel Clínico</span>
          </button>
          <span style="font-size:13px; font-weight:700; color:var(--color-mint);">Ajustes y Alarmas</span>
        </div>

        <!-- Clinical Target Range Configuration -->
        <div class="og-card">
          <h3 style="font-size:15px; font-weight:700; margin-bottom:4px;">Rango Clínico Objetivo (${state.unit})</h3>
          <p style="font-size:12px; color:var(--text-secondary); margin-bottom:14px;">
            Define los umbrales personalizados de hipoglucemia e hiperglucemia para ${p.firstName}.
          </p>
          <div style="display:grid; grid-template-columns:1fr 1fr; gap:14px;">
            <div>
              <label class="og-field-label">Umbral Bajo (mg/dL): <strong id="lbl-low">${p.targetLow}</strong></label>
              <input type="range" id="rng-target-low" min="60" max="95" value="${p.targetLow}" class="og-sim-slider" style="width:100%;" />
            </div>
            <div>
              <label class="og-field-label">Umbral Alto (mg/dL): <strong id="lbl-high">${p.targetHigh}</strong></label>
              <input type="range" id="rng-target-high" min="140" max="230" value="${p.targetHigh}" class="og-sim-slider" style="width:100%;" />
            </div>
          </div>
        </div>

        <!-- Clinical Alarms Section -->
        <div class="og-card">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
            <div>
              <h3 style="font-size:15px; font-weight:700;">Alarmas Clínicas Bidireccionales (Móvil + Wear OS)</h3>
              <p style="font-size:12px; color:var(--text-secondary);">Escalado sonoro progresivo e intervalos de 1 a 4 minutos</p>
            </div>
            <button type="button" id="og-test-alarm-btn" class="og-preset-btn" style="border-color:#EF4444; color:#EF4444;">
              Probar Alarma
            </button>
          </div>

          <div style="display:flex; flex-direction:column; gap:10px;">
            ${state.alarms.map((a, idx) => `
              <div style="display:flex; justify-content:space-between; align-items:center; padding:12px 14px; background:var(--surface-orb); border:1px solid var(--surface-border); border-radius:14px;">
                <div>
                  <div style="font-size:13.5px; font-weight:700;">${a.name} (${a.condition === 'below' ? '≤' : '>'} ${a.threshold} mg/dL)</div>
                  <div style="font-size:11.5px; color:var(--text-secondary);">
                    Tono: ${a.sound} · Repetición cada ${a.repeatMin} min ${a.escalate ? '· Escalado sonoro activo' : ''}
                  </div>
                </div>
                <input type="checkbox" class="og-alarm-toggle" data-idx="${idx}" ${a.enabled ? 'checked' : ''} style="width:18px; height:18px; accent-color:var(--color-mint); cursor:pointer;" />
              </div>
            `).join('')}
          </div>
        </div>

        <!-- Sensor Duration & Wear OS Pairing -->
        <div class="og-card" style="display:flex; flex-wrap:wrap; gap:10px; justify-content:space-between; align-items:center;">
          <div>
            <h3 style="font-size:15px; font-weight:700;">Emparejamiento Criptográfico Wear OS (ECDH)</h3>
            <p style="font-size:12px; color:var(--text-secondary);">Sincroniza el reloj inteligente mediante código QR firmado</p>
          </div>
          <button type="button" id="og-open-qr-screen" class="og-preset-btn" style="border-color:var(--color-mint); color:var(--color-mint); padding:8px 14px;">
            Mostrar QR Wear OS
          </button>
        </div>
      </div>
    `;
  }

  function renderMobileReports() {
    const p = getPatient();
    return `
      <div class="og-mobile-dashboard" style="max-width:680px;">
        <div class="og-top-appbar">
          <button type="button" class="og-patient-chip" id="og-back-dashboard-rep">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="19" y1="12" x2="5" y2="12"></line>
              <polyline points="12 19 5 12 12 5"></polyline>
            </svg>
            <span style="font-weight:700; font-size:14px;">Volver al Panel Clínico</span>
          </button>
          <span style="font-size:13px; font-weight:700; color:var(--color-mint);">Informes AGP y GDPR</span>
        </div>

        <div class="og-card">
          <h3 style="font-size:16px; font-weight:700; margin-bottom:6px;">Perfil Glucémico Ambulatorio (AGP) — ${p.firstName} ${p.lastName}</h3>
          <p style="font-size:12.5px; color:var(--text-secondary); margin-bottom:16px;">
            Distribución clínica de tiempo en rango según consenso internacional ATTD:
          </p>

          <div style="display:flex; height:24px; border-radius:12px; overflow:hidden; margin-bottom:12px; font-size:11px; font-weight:700; color:#000;">
            <div style="width:84%; background:#4ADE80; display:flex; align-items:center; justify-content:center;">84% En Rango (70-180)</div>
            <div style="width:12%; background:#FBBF24; display:flex; align-items:center; justify-content:center;">12%</div>
            <div style="width:4%; background:#F87171; display:flex; align-items:center; justify-content:center;">4%</div>
          </div>

          <div style="display:flex; gap:10px; flex-wrap:wrap; margin-top:20px;">
            <button type="button" id="og-export-csv-btn" class="og-btn-primary" style="flex:1; min-width:220px;">
              Descargar Historial CSV (GDPR Art. 20)
            </button>
          </div>
        </div>
      </div>
    `;
  }

  function renderMobileQr() {
    const p = getPatient();
    return `
      <div class="og-mobile-dashboard" style="max-width:480px; text-align:center;">
        <div class="og-top-appbar">
          <button type="button" class="og-patient-chip" id="og-back-settings-qr">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="19" y1="12" x2="5" y2="12"></line>
              <polyline points="12 19 5 12 12 5"></polyline>
            </svg>
            <span style="font-weight:700; font-size:14px;">Volver</span>
          </button>
          <span style="font-size:13px; font-weight:700; color:var(--color-mint);">Vinculación Wear OS</span>
        </div>

        <div class="og-card">
          <h3 style="font-size:16px; font-weight:700; margin-bottom:6px;">Código QR Criptográfico ECDH P-256</h3>
          <p style="font-size:12px; color:var(--text-secondary); margin-bottom:18px;">
            Escanea este código desde la app de OpenGluco en tu reloj Wear OS para transferir la sesión de <strong>${p.firstName}</strong> de forma cifrada.
          </p>

          <div style="width:190px; height:190px; margin:0 auto 16px; background:#FFF; padding:14px; border-radius:18px; display:flex; align-items:center; justify-content:center;">
            <svg viewBox="0 0 100 100" width="160" height="160" fill="#000">
              <rect x="5" y="5" width="28" height="28" fill="none" stroke="#000" stroke-width="6"/>
              <rect x="13" y="13" width="12" height="12"/>
              <rect x="67" y="5" width="28" height="28" fill="none" stroke="#000" stroke-width="6"/>
              <rect x="75" y="13" width="12" height="12"/>
              <rect x="5" y="67" width="28" height="28" fill="none" stroke="#000" stroke-width="6"/>
              <rect x="13" y="75" width="12" height="12"/>
              <rect x="42" y="12" width="8" height="8"/><rect x="54" y="20" width="8" height="16"/>
              <rect x="42" y="42" width="16" height="16"/><rect x="12" y="42" width="20" height="8"/>
              <rect x="66" y="46" width="24" height="8"/><rect x="44" y="66" width="12" height="24"/>
              <rect x="66" y="68" width="22" height="22" fill="none" stroke="#000" stroke-width="5"/>
            </svg>
          </div>

          <div style="font-family:'JetBrains Mono',monospace; font-size:11px; color:var(--color-mint); margin-bottom:14px;">
            SHA-256 Fingerprint: 8F:3A:91:C4:0E:7B:15:22
          </div>

          <button type="button" id="og-switch-to-wear" class="og-btn-outline">
            Abrir Vista de Reloj Wear OS Ahora →
          </button>
        </div>
      </div>
    `;
  }

  /* ==========================================================================
     WEAR OS SMARTWATCH VIEW (DESIGN_SYSTEM.md Section 3)
     ========================================================================== */
  function renderWearOsView() {
    const p = getPatient();
    const st = getClinicalStatus(p.currentMgDl, p.targetLow, p.targetHigh);
    const trend = getTrendMeta(p.trendArrow);
    const now = new Date();
    const timeStr = now.getHours().toString().padStart(2, '0') + ':' + now.getMinutes().toString().padStart(2, '0');

    return `
      <div style="display:flex; flex-direction:column; align-items:center; gap:18px; margin-top:10px;">
        <div class="og-wear-bezel">
          <!-- Curved Top TimeText -->
          <div style="font-family:'JetBrains Mono',monospace; font-size:13px; font-weight:700; color:#94A3B8; letter-spacing:1px;">
            ${timeStr} · ${p.firstName}
          </div>

          <!-- DualFloatingOrbs (76dp each) -->
          <div style="display:flex; gap:12px; align-items:center; justify-content:center;">
            <div id="wear-orb-left" style="width:94px; height:94px; border-radius:50%; background:#1E232D; border:2px solid ${st.color}; display:flex; flex-direction:column; align-items:center; justify-content:center; cursor:pointer;">
              <span style="font-family:'JetBrains Mono',monospace; font-size:28px; font-weight:800; line-height:1;">${formatGlucose(p.currentMgDl)}</span>
              <span style="font-size:10px; color:#94A3B8; margin-top:3px;">${state.unit}</span>
            </div>

            <div id="wear-orb-right" style="width:94px; height:94px; border-radius:50%; background:#1E232D; border:1px solid #2D3748; display:flex; flex-direction:column; align-items:center; justify-content:center; cursor:pointer;">
              <span style="font-size:26px; font-weight:800; line-height:1;">${trend.symbol}</span>
              <span style="font-size:9.5px; font-weight:700; color:${st.color}; margin-top:4px;">${trend.text}</span>
            </div>
          </div>

          <!-- Bottom Row: Sparkline + Sensor Pill -->
          <div style="display:flex; align-items:center; gap:8px; width:100%; justify-content:center;">
            <canvas id="og-wear-sparkline" width="145" height="44" style="width:145px; height:44px; background:#161A22; border-radius:12px; border:1px solid #2D3748;"></canvas>
            <div style="background:#1E232D; border:1px solid #2D3748; border-radius:12px; padding:6px 10px; text-align:center;">
              <div style="font-family:'JetBrains Mono',monospace; font-size:12px; font-weight:700; color:#4ADE80;">${p.sensor.remainingDays}d</div>
              <div style="font-size:8.5px; color:#94A3B8;">Sensor</div>
            </div>
          </div>

          <!-- Bottom Pill Action -->
          <button type="button" id="wear-trigger-sync" style="background:#1E232D; border:1px solid #2D3748; color:#94A3B8; font-size:10px; font-weight:700; padding:4px 14px; border-radius:99px; cursor:pointer;">
            Sincronizado BLE
          </button>
        </div>

        <p style="font-size:12.5px; color:var(--text-secondary); text-align:center; max-width:400px;">
          Interfaz circular <strong>Wear OS</strong> con esferas flotantes <code>DualFloatingOrbs</code>. Mueve el control deslizante superior para ver cómo reacciona el reloj en tiempo real.
        </p>
      </div>
    `;
  }

  /* ==========================================================================
     ANDROID AUTO HEADUNIT VIEW
     ========================================================================== */
  function renderAndroidAutoView() {
    const p = getPatient();
    const st = getClinicalStatus(p.currentMgDl, p.targetLow, p.targetHigh);
    const trend = getTrendMeta(p.trendArrow);

    return `
      <div class="og-auto-frame">
        <div style="display:flex; justify-content:space-between; align-items:center; border-bottom:1px solid #2D3748; padding-bottom:14px; margin-bottom:20px;">
          <div style="display:flex; align-items:center; gap:10px;">
            <img src="logo.png" alt="OpenGluco" style="width:28px; height:28px; border-radius:50%;" />
            <span style="font-weight:800; font-size:16px;">OpenGluco · Android Auto</span>
          </div>
          <span style="font-family:'JetBrains Mono',monospace; font-size:13px; color:#94A3B8;">
            Modo Conducción Segura · ${p.firstName} ${p.lastName}
          </span>
        </div>

        <div style="display:grid; grid-template-columns:1fr 1fr; gap:20px; align-items:center;">
          <div style="background:#161A22; border:2px solid ${st.color}; border-radius:22px; padding:26px; text-align:center;">
            <div style="font-size:13px; color:#94A3B8; font-weight:700; text-transform:uppercase;">Glucosa en Tiempo Real</div>
            <div style="font-family:'JetBrains Mono',monospace; font-size:64px; font-weight:800; color:${st.color}; line-height:1.05; margin:8px 0;">
              ${formatGlucose(p.currentMgDl)} <span style="font-size:48px;">${trend.symbol}</span>
            </div>
            <div style="font-size:15px; font-weight:700; color:#FFFFFF;">${state.unit} · ${st.label}</div>
          </div>

          <div style="display:flex; flex-direction:column; gap:12px;">
            <div style="background:#161A22; border:1px solid #2D3748; border-radius:18px; padding:18px;">
              <div style="font-size:12px; color:#94A3B8;">Cinética y Sensor</div>
              <div style="font-size:18px; font-weight:700; margin-top:4px;">${trend.text} (${trend.delta})</div>
              <div style="font-size:13px; color:#4ADE80; margin-top:6px;">
                ${p.sensor.modelName} · ${p.sensor.remainingDays} días restantes
              </div>
            </div>

            <button type="button" id="og-auto-tts-btn" class="og-btn-primary" style="height:56px; font-size:16px;">
              Lectura por Voz en Vehículo (TTS)
            </button>
          </div>
        </div>
      </div>
    `;
  }

  /* ==========================================================================
     CANVAS CHARTS (Continuous Bezier Curve & Wear Sparkline)
     ========================================================================== */
  function drawBezierChart() {
    const canvas = document.getElementById('og-bezier-canvas');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const w = canvas.width;
    const h = canvas.height;
    ctx.clearRect(0, 0, w, h);

    const p = getPatient();
    const data = p.historyMgDl;
    if (!data || data.length < 2) return;

    const minY = 40;
    const maxY = 260;
    const padX = 28;
    const padY = 20;

    const mapX = (i) => padX + (i / (data.length - 1)) * (w - padX * 2);
    const mapY = (val) => h - padY - ((Math.max(minY, Math.min(maxY, val)) - minY) / (maxY - minY)) * (h - padY * 2);

    // Target zone band (targetLow .. targetHigh)
    const yHigh = mapY(p.targetHigh);
    const yLow = mapY(p.targetLow);
    ctx.fillStyle = 'rgba(74, 222, 128, 0.07)';
    ctx.fillRect(padX, yHigh, w - padX * 2, yLow - yHigh);

    ctx.strokeStyle = 'rgba(74, 222, 128, 0.35)';
    ctx.setLineDash([5, 5]);
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(padX, yHigh);
    ctx.lineTo(w - padX, yHigh);
    ctx.moveTo(padX, yLow);
    ctx.lineTo(w - padX, yLow);
    ctx.stroke();
    ctx.setLineDash([]);

    // Smooth Bezier curve
    const st = getClinicalStatus(p.currentMgDl, p.targetLow, p.targetHigh);
    const pts = data.map((v, i) => ({ x: mapX(i), y: mapY(v), v }));

    ctx.beginPath();
    ctx.moveTo(pts[0].x, pts[0].y);
    for (let i = 0; i < pts.length - 1; i++) {
      const xc = (pts[i].x + pts[i + 1].x) / 2;
      const yc = (pts[i].y + pts[i + 1].y) / 2;
      ctx.quadraticCurveTo(pts[i].x, pts[i].y, xc, yc);
    }
    ctx.lineTo(pts[pts.length - 1].x, pts[pts.length - 1].y);

    ctx.strokeStyle = st.color;
    ctx.lineWidth = 3;
    ctx.stroke();

    // Area gradient under curve
    ctx.lineTo(pts[pts.length - 1].x, h - padY);
    ctx.lineTo(pts[0].x, h - padY);
    ctx.closePath();
    const grad = ctx.createLinearGradient(0, 0, 0, h);
    grad.addColorStop(0, st.bg);
    grad.addColorStop(1, 'rgba(0,0,0,0)');
    ctx.fillStyle = grad;
    ctx.fill();

    // Draw points
    pts.forEach((pt, idx) => {
      ctx.beginPath();
      ctx.arc(pt.x, pt.y, idx === pts.length - 1 ? 5 : 3, 0, Math.PI * 2);
      ctx.fillStyle = idx === pts.length - 1 ? '#FFFFFF' : st.color;
      ctx.fill();
    });
  }

  function drawWearSparkline() {
    const canvas = document.getElementById('og-wear-sparkline');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const w = canvas.width;
    const h = canvas.height;
    ctx.clearRect(0, 0, w, h);

    const p = getPatient();
    const data = p.historyMgDl.slice(-10);
    const st = getClinicalStatus(p.currentMgDl, p.targetLow, p.targetHigh);
    const pts = data.map((v, i) => ({
      x: 8 + (i / (data.length - 1)) * (w - 16),
      y: h - 6 - ((Math.max(50, Math.min(240, v)) - 50) / 190) * (h - 12)
    }));

    ctx.beginPath();
    ctx.moveTo(pts[0].x, pts[0].y);
    for (let i = 1; i < pts.length; i++) ctx.lineTo(pts[i].x, pts[i].y);
    ctx.strokeStyle = st.color;
    ctx.lineWidth = 2.2;
    ctx.stroke();
  }

  /* ==========================================================================
     MODALS (Patient Selector & Sensor 14d/15d Selector)
     ========================================================================== */
  function showModal(htmlContent) {
    const modalRoot = document.getElementById('og-modal-root');
    if (!modalRoot) return;
    modalRoot.innerHTML = `
      <div class="og-modal-backdrop" id="og-modal-backdrop">
        <div class="og-modal" onclick="event.stopPropagation()">
          ${htmlContent}
        </div>
      </div>
    `;
    document.getElementById('og-modal-backdrop').addEventListener('click', closeModal);
  }

  function closeModal() {
    const modalRoot = document.getElementById('og-modal-root');
    if (modalRoot) modalRoot.innerHTML = '';
  }

  function openPatientSelectorModal() {
    showModal(`
      <h3 style="font-size:17px; font-weight:800; margin-bottom:6px;">Seleccionar Paciente Conectado</h3>
      <p style="font-size:12.5px; color:var(--text-secondary); margin-bottom:16px;">
        Conexiones activas mediante LibreLinkUp Cloud:
      </p>
      <div style="display:flex; flex-direction:column; gap:10px;">
        ${PATIENTS.map((pt, i) => `
          <button type="button" class="og-preset-btn" data-select-pt="${i}" style="display:flex; justify-content:space-between; align-items:center; padding:14px; text-align:left; border-color:${state.selectedPatientIdx === i ? 'var(--color-mint)' : 'var(--surface-border)'};">
            <div>
              <div style="font-size:14px; font-weight:700;">${pt.firstName} ${pt.lastName}</div>
              <div style="font-size:11.5px; color:var(--text-secondary);">${pt.sensor.modelName} · ${pt.role}</div>
            </div>
            <span style="font-family:'JetBrains Mono',monospace; font-weight:700; color:var(--color-mint);">
              ${formatGlucose(pt.currentMgDl)} ${state.unit}
            </span>
          </button>
        `).join('')}
      </div>
    `);

    document.querySelectorAll('[data-select-pt]').forEach(btn => {
      btn.addEventListener('click', () => {
        state.selectedPatientIdx = parseInt(btn.getAttribute('data-select-pt'), 10);
        closeModal();
        render();
      });
    });
  }

  function openSensorDurationModal() {
    const p = getPatient();
    showModal(`
      <h3 style="font-size:17px; font-weight:800; margin-bottom:6px;">Configuración Clínica del Sensor</h3>
      <p style="font-size:12.5px; color:var(--text-secondary); margin-bottom:16px;">
        Selecciona la duración nominal del sensor activo (soporte para sensores de 14 días y nuevos sensores Plus de 15 días):
      </p>
      <div style="display:grid; grid-template-columns:1fr 1fr; gap:10px; margin-bottom:16px;">
        <button type="button" class="og-preset-btn" id="btn-dur-14" style="padding:14px; border-color:${p.sensor.lifetimeDays === 14 ? 'var(--color-mint)' : 'var(--surface-border)'};">
          <div style="font-size:15px; font-weight:800;">14 Días</div>
          <div style="font-size:11px; color:var(--text-secondary);">FreeStyle Libre 2 / 3</div>
        </button>
        <button type="button" class="og-preset-btn" id="btn-dur-15" style="padding:14px; border-color:${p.sensor.lifetimeDays === 15 ? 'var(--color-mint)' : 'var(--surface-border)'};">
          <div style="font-size:15px; font-weight:800;">15 Días (Plus)</div>
          <div style="font-size:11px; color:var(--text-secondary);">Libre 2 Plus / 3 Plus</div>
        </button>
      </div>
      <div style="font-size:12px; color:var(--text-secondary); margin-bottom:16px;">
        Número de serie: <strong style="font-family:'JetBrains Mono',monospace;">${p.sensor.serialNumber}</strong>
      </div>
      <button type="button" class="og-btn-primary" id="btn-close-sensor-modal">Guardar y Cerrar</button>
    `);

    document.getElementById('btn-dur-14').addEventListener('click', () => {
      p.sensor.lifetimeDays = 14;
      p.sensor.modelName = 'FreeStyle Libre 3 (14d)';
      p.sensor.remainingDays = Math.min(p.sensor.remainingDays, 14);
      closeModal();
      render();
    });
    document.getElementById('btn-dur-15').addEventListener('click', () => {
      p.sensor.lifetimeDays = 15;
      p.sensor.modelName = 'FreeStyle Libre 3 Plus (15d)';
      p.sensor.remainingDays = 12;
      closeModal();
      render();
    });
    document.getElementById('btn-close-sensor-modal').addEventListener('click', closeModal);
  }

  /* ==========================================================================
     EVENT BINDINGS
     ========================================================================== */
  function bindLoginEvents() {
    const form = document.getElementById('og-login-form');
    if (form) {
      form.addEventListener('submit', (e) => {
        e.preventDefault();
        state.isAuthenticated = true;
        render();
      });
    }
    const btn1 = document.getElementById('og-demo-patient-1');
    if (btn1) {
      btn1.addEventListener('click', () => {
        state.selectedPatientIdx = 0;
        state.isAuthenticated = true;
        render();
      });
    }
    const btn2 = document.getElementById('og-demo-patient-2');
    if (btn2) {
      btn2.addEventListener('click', () => {
        state.selectedPatientIdx = 1;
        state.isAuthenticated = true;
        render();
      });
    }
  }

  function bindDashboardEvents() {
    const ptBtn = document.getElementById('og-open-patient-modal');
    if (ptBtn) ptBtn.addEventListener('click', openPatientSelectorModal);

    const sensorCard = document.getElementById('og-sensor-card');
    if (sensorCard) sensorCard.addEventListener('click', openSensorDurationModal);

    const btnRefresh = document.getElementById('og-btn-refresh');
    if (btnRefresh) {
      btnRefresh.addEventListener('click', () => {
        const p = getPatient();
        const jitter = (Math.random() > 0.5 ? 1 : -1) * Math.floor(Math.random() * 4);
        setLiveGlucose(p.currentMgDl + jitter, p.trendArrow);
      });
    }

    const btnSettings = document.getElementById('og-btn-settings');
    if (btnSettings) {
      btnSettings.addEventListener('click', () => {
        state.mobileSubScreen = 'settings';
        render();
      });
    }

    const btnReports = document.getElementById('og-btn-reports');
    if (btnReports) {
      btnReports.addEventListener('click', () => {
        state.mobileSubScreen = 'reports';
        render();
      });
    }

    const btnLogout = document.getElementById('og-btn-logout');
    if (btnLogout) {
      btnLogout.addEventListener('click', () => {
        state.isAuthenticated = false;
        render();
      });
    }

    const dismissBtn = document.getElementById('og-dismiss-alarm');
    if (dismissBtn) {
      dismissBtn.addEventListener('click', () => {
        state.alarmTriggered = null;
        render();
      });
    }

    document.querySelectorAll('[data-tf]').forEach(btn => {
      btn.addEventListener('click', () => {
        state.timeframeHours = parseInt(btn.getAttribute('data-tf'), 10);
        render();
      });
    });

    document.querySelectorAll('[data-period]').forEach(btn => {
      btn.addEventListener('click', () => {
        state.statsPeriodDays = parseInt(btn.getAttribute('data-period'), 10);
        render();
      });
    });
  }

  function bindSettingsEvents() {
    const backBtn = document.getElementById('og-back-dashboard');
    if (backBtn) {
      backBtn.addEventListener('click', () => {
        state.mobileSubScreen = 'dashboard';
        render();
      });
    }

    const p = getPatient();
    const rngLow = document.getElementById('rng-target-low');
    const rngHigh = document.getElementById('rng-target-high');
    if (rngLow) {
      rngLow.addEventListener('input', () => {
        p.targetLow = parseInt(rngLow.value, 10);
        document.getElementById('lbl-low').textContent = p.targetLow;
      });
    }
    if (rngHigh) {
      rngHigh.addEventListener('input', () => {
        p.targetHigh = parseInt(rngHigh.value, 10);
        document.getElementById('lbl-high').textContent = p.targetHigh;
      });
    }

    document.querySelectorAll('.og-alarm-toggle').forEach(chk => {
      chk.addEventListener('change', () => {
        const idx = parseInt(chk.getAttribute('data-idx'), 10);
        state.alarms[idx].enabled = chk.checked;
      });
    });

    const testAlarmBtn = document.getElementById('og-test-alarm-btn');
    if (testAlarmBtn) {
      testAlarmBtn.addEventListener('click', () => {
        state.mobileSubScreen = 'dashboard';
        setLiveGlucose(52, 1);
      });
    }

    const openQrBtn = document.getElementById('og-open-qr-screen');
    if (openQrBtn) {
      openQrBtn.addEventListener('click', () => {
        state.mobileSubScreen = 'qr';
        render();
      });
    }
  }

  function bindReportsEvents() {
    const backBtn = document.getElementById('og-back-dashboard-rep');
    if (backBtn) {
      backBtn.addEventListener('click', () => {
        state.mobileSubScreen = 'dashboard';
        render();
      });
    }

    const csvBtn = document.getElementById('og-export-csv-btn');
    if (csvBtn) {
      csvBtn.addEventListener('click', () => {
        const p = getPatient();
        const rows = ['Timestamp,Paciente,Glucosa_mg_dL,Tendencia,Sensor_SN'];
        p.historyMgDl.forEach((v, idx) => {
          const d = new Date(Date.now() - (p.historyMgDl.length - idx) * 15 * 60000).toISOString();
          rows.push(`${d},${p.firstName} ${p.lastName},${v},${getTrendMeta(p.trendArrow).symbol},${p.sensor.serialNumber}`);
        });
        const blob = new Blob([rows.join('\n')], { type: 'text/csv;charset=utf-8;' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `OpenGluco_${p.firstName}_${p.lastName}_GDPR.csv`;
        a.click();
        URL.revokeObjectURL(url);
      });
    }
  }

  function bindQrEvents() {
    const backBtn = document.getElementById('og-back-settings-qr');
    if (backBtn) {
      backBtn.addEventListener('click', () => {
        state.mobileSubScreen = 'settings';
        render();
      });
    }
    const wearBtn = document.getElementById('og-switch-to-wear');
    if (wearBtn) {
      wearBtn.addEventListener('click', () => {
        setActiveDeviceTab('wear');
      });
    }
  }

  function bindWearEvents() {
    const syncBtn = document.getElementById('wear-trigger-sync');
    if (syncBtn) {
      syncBtn.addEventListener('click', () => {
        syncBtn.textContent = 'Telemetría Actualizada';
        setTimeout(() => {
          if (syncBtn) syncBtn.textContent = 'Sincronizado BLE';
        }, 1200);
      });
    }
  }

  function bindAutoEvents() {
    const ttsBtn = document.getElementById('og-auto-tts-btn');
    if (ttsBtn) {
      ttsBtn.addEventListener('click', () => {
        const p = getPatient();
        const trend = getTrendMeta(p.trendArrow);
        const msg = `OpenGluco: Nivel de glucosa de ${p.firstName}, ${formatGlucose(p.currentMgDl)} ${state.unit === 'mg/dL' ? 'miligramos por decilitro' : 'milimoles por litro'}, tendencia ${trend.text}.`;
        if ('speechSynthesis' in window) {
          window.speechSynthesis.cancel();
          const utter = new SpeechSynthesisUtterance(msg);
          utter.lang = 'es-ES';
          window.speechSynthesis.speak(utter);
        }
      });
    }
  }

  function setActiveDeviceTab(device) {
    state.activeDevice = device;
    document.querySelectorAll('.og-device-tab').forEach(t => {
      t.classList.toggle('active', t.getAttribute('data-device') === device);
    });
    render();
  }

  /* ==========================================================================
     GLOBAL HEADER & SIMULATOR STRIP EVENTS
     ========================================================================== */
  function initGlobalEvents() {
    document.querySelectorAll('.og-device-tab').forEach(tab => {
      tab.addEventListener('click', () => {
        setActiveDeviceTab(tab.getAttribute('data-device'));
      });
    });

    const unitBtn = document.getElementById('og-unit-toggle');
    if (unitBtn) {
      unitBtn.addEventListener('click', () => {
        state.unit = state.unit === 'mg/dL' ? 'mmol/L' : 'mg/dL';
        unitBtn.textContent = state.unit;
        render();
      });
    }

    const themeBtn = document.getElementById('og-theme-toggle');
    if (themeBtn) {
      themeBtn.addEventListener('click', () => {
        state.isDark = !state.isDark;
        document.documentElement.classList.toggle('light-theme', !state.isDark);
        themeBtn.textContent = state.isDark ? 'OLED' : 'Claro';
        render();
      });
    }

    const slider = document.getElementById('og-glucose-slider');
    if (slider) {
      slider.addEventListener('input', () => {
        const val = parseInt(slider.value, 10);
        const p = getPatient();
        const diff = val - p.currentMgDl;
        const arrow = diff > 15 ? 5 : diff > 4 ? 4 : diff < -15 ? 1 : diff < -4 ? 2 : 3;
        setLiveGlucose(val, arrow);
      });
    }

    const presets = {
      normal: { v: 114, a: 3 },
      rising: { v: 164, a: 4 },
      high: { v: 218, a: 5 },
      low: { v: 63, a: 2 },
      urgent: { v: 51, a: 1 }
    };

    document.querySelectorAll('[data-preset]').forEach(btn => {
      btn.addEventListener('click', () => {
        const key = btn.getAttribute('data-preset');
        if (presets[key]) {
          setLiveGlucose(presets[key].v, presets[key].a);
        }
      });
    });
  }

  document.addEventListener('DOMContentLoaded', () => {
    initGlobalEvents();
    render();
  });
})();
