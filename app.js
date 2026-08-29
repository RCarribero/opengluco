// OpenGluco Interactive 3D Medical DNA & Molecular Physics Engine
// Optimized Pointer Event Drag Physics & Dynamic Theme Color Shift

const GLUCOSE_STATES = {
  in_range: {
    title: 'En Rango',
    value: 114,
    trend: '\u2192',
    trendLabel: 'Estable',
    delta: '+0.2 mg/dL/min',
    darkColor: '#4ADE80',
    darkSecondary: '#38BDF8',
    darkRgb: '74, 222, 128',
    lightColor: '#059669',
    lightSecondary: '#2563EB',
    lightRgb: '5, 150, 105',
    rangeLabel: '70 - 180 mg/dL'
  },
  low: {
    title: 'Nivel Bajo',
    value: 64,
    trend: '\u2193',
    trendLabel: 'Bajando',
    delta: '-1.6 mg/dL/min',
    darkColor: '#F87171',
    darkSecondary: '#FB7185',
    darkRgb: '248, 113, 113',
    lightColor: '#dc2626',
    lightSecondary: '#e11d48',
    lightRgb: '220, 38, 38',
    rangeLabel: '56 - 69 mg/dL'
  },
  urgent_low: {
    title: 'Urgente Bajo',
    value: 48,
    trend: '\u2193\u2193',
    trendLabel: 'Caida Rapida',
    delta: '-3.1 mg/dL/min',
    darkColor: '#EF4444',
    darkSecondary: '#F43F5E',
    darkRgb: '239, 68, 68',
    lightColor: '#b91c1c',
    lightSecondary: '#be123c',
    lightRgb: '185, 28, 28',
    rangeLabel: '<= 55 mg/dL'
  },
  high: {
    title: 'Nivel Alto',
    value: 205,
    trend: '\u2191',
    trendLabel: 'Subiendo',
    delta: '+2.0 mg/dL/min',
    darkColor: '#FBBF24',
    darkSecondary: '#F59E0B',
    darkRgb: '251, 191, 36',
    lightColor: '#d97706',
    lightSecondary: '#b45309',
    lightRgb: '217, 119, 6',
    rangeLabel: '181 - 249 mg/dL'
  },
  very_high: {
    title: 'Muy Alto',
    value: 270,
    trend: '\u2191\u2191',
    trendLabel: 'Subida Rapida',
    delta: '+4.2 mg/dL/min',
    darkColor: '#FB923C',
    darkSecondary: '#EA580C',
    darkRgb: '251, 146, 60',
    lightColor: '#ea580c',
    lightSecondary: '#c2410c',
    lightRgb: '234, 88, 12',
    rangeLabel: '>= 250 mg/dL'
  }
};

let currentRangeKey = 'in_range';
let isLightTheme = false;

// -----------------------------------------------------------
// 3D MEDICAL DNA & MOLECULAR SCENE
// -----------------------------------------------------------
class InteractiveMedical3DScene {
  constructor(canvasId) {
    this.canvas = document.getElementById(canvasId);
    if (!this.canvas) return;
    if (typeof THREE === 'undefined') return;

    this.scene = null;
    this.camera = null;
    this.renderer = null;
    this.helixGroup = null;
    this.moleculeGroup = null;
    this.nodesA = [];
    this.nodesB = [];
    this.rungs = [];
    this.molecules = [];
    this.coreLight = null;
    this.ambientLight = null;
    this.dirLight1 = null;
    this.dirLight2 = null;
    this.particles = null;

    // Rock-solid Drag Physics State (immune to getting stuck)
    this.isDragging = false;
    this.prevPointer = { x: 0, y: 0 };
    this.rotVelocity = { x: 0, y: 0.006 };
    this.currentRotation = { x: 0.25, y: 0 };
    this.scrollY = 0;

    this.init();
  }

  init() {
    const width = window.innerWidth;
    const height = window.innerHeight;

    this.scene = new THREE.Scene();
    this.camera = new THREE.PerspectiveCamera(45, width / height, 0.1, 1000);
    this.camera.position.set(0, 0, 9.0);

    try {
      this.renderer = new THREE.WebGLRenderer({
        canvas: this.canvas,
        antialias: true,
        alpha: true,
        powerPreference: 'high-performance'
      });
      this.renderer.setSize(width, height);
      this.renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
      this.renderer.setClearColor(0x000000, 0);
    } catch (e) {
      console.error(e);
      return;
    }

    this.ambientLight = new THREE.AmbientLight(0xffffff, 1.2);
    this.scene.add(this.ambientLight);

    this.dirLight1 = new THREE.DirectionalLight(0xffffff, 2.2);
    this.dirLight1.position.set(6, 12, 8);
    this.scene.add(this.dirLight1);

    this.dirLight2 = new THREE.DirectionalLight(0x38bdf8, 1.2);
    this.dirLight2.position.set(-6, -8, -4);
    this.scene.add(this.dirLight2);

    this.coreLight = new THREE.PointLight(0x4ade80, 6.0, 30);
    this.coreLight.position.set(2.0, 0, 3.0);
    this.scene.add(this.coreLight);

    this.buildDNAHelix();
    this.buildGlucoseMolecules();
    this.buildParticles();
    this.updateLayout();
    this.setupInteractions();

    this.animate = this.animate.bind(this);
    requestAnimationFrame(this.animate);
  }

  buildDNAHelix() {
    this.helixGroup = new THREE.Group();
    const pairCount = 36;
    const heightSpan = 15;
    const radius = 2.0;
    const turns = 2.8;

    this.nodeMatA = new THREE.MeshStandardMaterial({
      color: 0x0f172a,
      emissive: 0x4ade80,
      emissiveIntensity: 1.2,
      roughness: 0.15,
      metalness: 0.8
    });

    this.nodeMatB = new THREE.MeshStandardMaterial({
      color: 0x0f172a,
      emissive: 0x38bdf8,
      emissiveIntensity: 1.0,
      roughness: 0.15,
      metalness: 0.8
    });

    this.rungMat = new THREE.MeshStandardMaterial({
      color: 0x94a3b8,
      metalness: 0.9,
      roughness: 0.2,
      transparent: true,
      opacity: 0.9
    });

    const sphereGeo = new THREE.SphereGeometry(0.28, 24, 24);
    const spinePointsA = [];
    const spinePointsB = [];

    for (let i = 0; i < pairCount; i++) {
      const t = i / (pairCount - 1);
      const y = (t - 0.5) * heightSpan;
      const angle = t * Math.PI * 2 * turns;

      const x1 = Math.cos(angle) * radius;
      const z1 = Math.sin(angle) * radius;
      const x2 = Math.cos(angle + Math.PI) * radius;
      const z2 = Math.sin(angle + Math.PI) * radius;

      spinePointsA.push(new THREE.Vector3(x1, y, z1));
      spinePointsB.push(new THREE.Vector3(x2, y, z2));

      const nodeA = new THREE.Mesh(sphereGeo, this.nodeMatA);
      nodeA.position.set(x1, y, z1);
      this.helixGroup.add(nodeA);
      this.nodesA.push(nodeA);

      const nodeB = new THREE.Mesh(sphereGeo, this.nodeMatB);
      nodeB.position.set(x2, y, z2);
      this.helixGroup.add(nodeB);
      this.nodesB.push(nodeB);

      const p1 = new THREE.Vector3(x1, y, z1);
      const p2 = new THREE.Vector3(x2, y, z2);
      const dist = p1.distanceTo(p2);

      const rungGeo = new THREE.CylinderGeometry(0.055, 0.055, dist, 12);
      const rungMesh = new THREE.Mesh(rungGeo, this.rungMat);
      const mid = new THREE.Vector3().addVectors(p1, p2).multiplyScalar(0.5);
      rungMesh.position.copy(mid);
      rungMesh.quaternion.setFromUnitVectors(new THREE.Vector3(0, 1, 0), p2.clone().sub(p1).normalize());

      this.helixGroup.add(rungMesh);
      this.rungs.push(rungMesh);
    }

    const curveA = new THREE.CatmullRomCurve3(spinePointsA);
    const tubeGeoA = new THREE.TubeGeometry(curveA, 100, 0.06, 8, false);
    this.tubeMeshA = new THREE.Mesh(tubeGeoA, this.nodeMatA);
    this.helixGroup.add(this.tubeMeshA);

    const curveB = new THREE.CatmullRomCurve3(spinePointsB);
    const tubeGeoB = new THREE.TubeGeometry(curveB, 100, 0.06, 8, false);
    this.tubeMeshB = new THREE.Mesh(tubeGeoB, this.nodeMatB);
    this.helixGroup.add(this.tubeMeshB);

    this.helixGroup.rotation.z = -0.22;
    this.helixGroup.rotation.x = 0.28;
    this.scene.add(this.helixGroup);
  }

  buildGlucoseMolecules() {
    this.moleculeGroup = new THREE.Group();
    const count = 8;

    this.bondMat = new THREE.MeshStandardMaterial({
      color: 0xcfd8dc,
      metalness: 0.8,
      roughness: 0.2
    });

    this.atomMat = new THREE.MeshStandardMaterial({
      color: 0x0f172a,
      emissive: 0x4ade80,
      emissiveIntensity: 1.1,
      metalness: 0.8,
      roughness: 0.2
    });

    const atomGeo = new THREE.SphereGeometry(0.16, 16, 16);
    const bondGeo = new THREE.CylinderGeometry(0.035, 0.035, 0.55, 8);

    for (let m = 0; m < count; m++) {
      const mol = new THREE.Group();
      const ringRadius = 0.55;

      for (let i = 0; i < 6; i++) {
        const ang = (i / 6) * Math.PI * 2;
        const nextAng = ((i + 1) / 6) * Math.PI * 2;

        const ax = Math.cos(ang) * ringRadius;
        const ay = Math.sin(ang) * ringRadius;
        const nx = Math.cos(nextAng) * ringRadius;
        const ny = Math.sin(nextAng) * ringRadius;

        const atom = new THREE.Mesh(atomGeo, this.atomMat);
        atom.position.set(ax, ay, 0);
        mol.add(atom);

        const bond = new THREE.Mesh(bondGeo, this.bondMat);
        const p1 = new THREE.Vector3(ax, ay, 0);
        const p2 = new THREE.Vector3(nx, ny, 0);
        const mid = new THREE.Vector3().addVectors(p1, p2).multiplyScalar(0.5);
        bond.position.copy(mid);
        bond.quaternion.setFromUnitVectors(new THREE.Vector3(0, 1, 0), p2.sub(p1).normalize());
        mol.add(bond);
      }

      const theta = (m / count) * Math.PI * 2;
      const orbitR = 3.2 + Math.random() * 2.0;
      mol.position.set(
        Math.cos(theta) * orbitR,
        (Math.random() - 0.5) * 10.0,
        Math.sin(theta) * orbitR
      );

      mol.userData = {
        orbitSpeed: 0.22 + Math.random() * 0.28,
        orbitRadius: orbitR,
        baseTheta: theta,
        baseY: mol.position.y,
        rotSpeedX: 0.6 + Math.random() * 0.6,
        rotSpeedY: 0.6 + Math.random() * 0.6
      };

      this.molecules.push(mol);
      this.moleculeGroup.add(mol);
    }

    this.scene.add(this.moleculeGroup);
  }

  buildParticles() {
    const count = 450;
    const geo = new THREE.BufferGeometry();
    const positions = new Float32Array(count * 3);

    for (let i = 0; i < count; i++) {
      const radius = 1.8 + Math.random() * 7.5;
      const theta = Math.random() * Math.PI * 2;
      const phi = Math.acos(Math.random() * 2 - 1);

      positions[i * 3] = radius * Math.sin(phi) * Math.cos(theta);
      positions[i * 3 + 1] = (Math.random() - 0.5) * 18;
      positions[i * 3 + 2] = radius * Math.cos(phi);
    }

    geo.setAttribute('position', new THREE.BufferAttribute(positions, 3));

    this.particleMat = new THREE.PointsMaterial({
      color: 0x4ade80,
      size: 0.08,
      transparent: true,
      opacity: 0.75,
      blending: THREE.AdditiveBlending
    });

    this.particles = new THREE.Points(geo, this.particleMat);
    this.scene.add(this.particles);
  }

  updateLayout() {
    const isWide = window.innerWidth >= 1024;
    const targetX = isWide ? 2.2 : 0.0;
    if (this.helixGroup) this.helixGroup.position.x = targetX;
    if (this.moleculeGroup) this.moleculeGroup.position.x = targetX;
    if (this.particles) this.particles.position.x = targetX;
    if (this.coreLight) this.coreLight.position.x = targetX;
  }

  setupInteractions() {
    const onStart = (cx, cy) => {
      this.isDragging = true;
      this.prevPointer = { x: cx, y: cy };
      this.rotVelocity = { x: 0, y: 0 };
    };

    const onMove = (cx, cy, buttons) => {
      // Safety release check: if mouse buttons are 0, force release drag
      if (buttons === 0 && this.isDragging) {
        this.isDragging = false;
        return;
      }

      if (this.isDragging) {
        const dx = cx - this.prevPointer.x;
        const dy = cy - this.prevPointer.y;
        this.rotVelocity = { x: dy * 0.004, y: dx * 0.004 };
        this.currentRotation.y += dx * 0.007;
        this.currentRotation.x += dy * 0.007;
        this.prevPointer = { x: cx, y: cy };
      }
    };

    const onEnd = () => {
      this.isDragging = false;
    };

    // Mouse Listeners
    window.addEventListener('mousedown', (e) => {
      if (e.button === 0) onStart(e.clientX, e.clientY);
    });
    window.addEventListener('mousemove', (e) => onMove(e.clientX, e.clientY, e.buttons));
    window.addEventListener('mouseup', onEnd);
    window.addEventListener('mouseleave', onEnd);
    window.addEventListener('blur', onEnd);

    // Touch Listeners
    window.addEventListener('touchstart', (e) => {
      if (e.touches.length === 1) onStart(e.touches[0].clientX, e.touches[0].clientY);
    }, { passive: true });
    window.addEventListener('touchmove', (e) => {
      if (e.touches.length === 1) onMove(e.touches[0].clientX, e.touches[0].clientY, 1);
    }, { passive: true });
    window.addEventListener('touchend', onEnd);
    window.addEventListener('touchcancel', onEnd);

    window.addEventListener('scroll', () => {
      this.scrollY = window.scrollY || window.pageYOffset;
    }, { passive: true });

    window.addEventListener('resize', () => {
      if (!this.renderer || !this.camera) return;
      const w = window.innerWidth;
      const h = window.innerHeight;
      this.camera.aspect = w / h;
      this.camera.updateProjectionMatrix();
      this.renderer.setSize(w, h);
      this.updateLayout();
    });
  }

  setTheme(lightMode) {
    if (lightMode) {
      // Light Mode Color Palette: Crystal sapphire, gold and emerald
      this.ambientLight.intensity = 1.4;
      if (this.dirLight1) this.dirLight1.intensity = 1.8;
      if (this.dirLight2) this.dirLight2.color.setHex(0x2563eb);
      this.nodeMatA.color.setHex(0xf1f5f9);
      this.nodeMatB.color.setHex(0xf1f5f9);
      this.rungMat.color.setHex(0x64748b);
      this.atomMat.color.setHex(0xf8fafc);
      this.particleMat.opacity = 0.55;
    } else {
      // Dark Mode Color Palette: Bioluminescent cyber-glow
      this.ambientLight.intensity = 1.0;
      if (this.dirLight1) this.dirLight1.intensity = 2.2;
      if (this.dirLight2) this.dirLight2.color.setHex(0x38bdf8);
      this.nodeMatA.color.setHex(0x0f172a);
      this.nodeMatB.color.setHex(0x0f172a);
      this.rungMat.color.setHex(0x94a3b8);
      this.atomMat.color.setHex(0x0f172a);
      this.particleMat.opacity = 0.75;
    }
    this.updateColors();
  }

  updateColors() {
    const data = GLUCOSE_STATES[currentRangeKey];
    const primaryHex = isLightTheme ? data.lightColor : data.darkColor;
    const secondaryHex = isLightTheme ? data.lightSecondary : data.darkSecondary;

    const c1 = new THREE.Color(primaryHex);
    const c2 = new THREE.Color(secondaryHex);

    if (this.nodeMatA) {
      this.nodeMatA.emissive.copy(c1);
      this.nodeMatA.emissiveIntensity = isLightTheme ? 1.0 : 1.3;
    }
    if (this.nodeMatB) {
      this.nodeMatB.emissive.copy(c2);
      this.nodeMatB.emissiveIntensity = isLightTheme ? 0.9 : 1.1;
    }
    if (this.atomMat) {
      this.atomMat.emissive.copy(c1);
      this.atomMat.emissiveIntensity = isLightTheme ? 0.85 : 1.1;
    }
    if (this.coreLight) {
      this.coreLight.color.copy(c1);
    }
    if (this.particleMat) {
      this.particleMat.color.copy(c1);
    }
  }

  animate(time) {
    requestAnimationFrame(this.animate);
    const t = time * 0.001;

    if (!this.isDragging) {
      this.currentRotation.y += 0.004 + this.rotVelocity.y;
      this.currentRotation.x += this.rotVelocity.x;
      this.rotVelocity.x *= 0.93;
      this.rotVelocity.y *= 0.93;
    }

    const scrollFactor = Math.min(1, this.scrollY / 1200);
    const scrollOffsetY = -scrollFactor * 2.2;
    const scrollOffsetZ = scrollFactor * 1.2;

    if (this.helixGroup) {
      this.helixGroup.rotation.y = this.currentRotation.y;
      this.helixGroup.rotation.x = this.currentRotation.x;
      this.helixGroup.position.y = 0.2 + Math.sin(t * 1.2) * 0.15 + scrollOffsetY;
      this.helixGroup.position.z = scrollOffsetZ;
    }

    if (this.moleculeGroup) {
      this.molecules.forEach((mol) => {
        const speed = mol.userData.orbitSpeed;
        const angle = mol.userData.baseTheta + t * speed + this.currentRotation.y * 0.4;
        const r = mol.userData.orbitRadius;
        mol.position.x = Math.cos(angle) * r;
        mol.position.z = Math.sin(angle) * r;
        mol.position.y = mol.userData.baseY + Math.sin(t * 1.5 + mol.userData.baseTheta) * 0.35 + scrollOffsetY;
        mol.rotation.x += mol.userData.rotSpeedX * 0.02;
        mol.rotation.y += mol.userData.rotSpeedY * 0.02;
      });
    }

    if (this.coreLight) {
      this.coreLight.intensity = (isLightTheme ? 4.5 : 6.0) + Math.sin(t * 2.5) * 1.5;
    }

    if (this.particles) {
      this.particles.rotation.y = t * 0.02 + this.currentRotation.y * 0.2;
      this.particles.rotation.x = t * 0.01;
    }

    this.renderer.render(this.scene, this.camera);
  }
}

let medical3DScene = null;

function setTheme(light) {
  isLightTheme = light;
  const html = document.documentElement;
  const moonIcon = document.getElementById('icon-moon');
  const sunIcon = document.getElementById('icon-sun');

  if (isLightTheme) {
    html.classList.add('light');
    if (moonIcon) moonIcon.classList.remove('hidden');
    if (sunIcon) sunIcon.classList.add('hidden');
  } else {
    html.classList.remove('light');
    if (moonIcon) moonIcon.classList.add('hidden');
    if (sunIcon) sunIcon.classList.remove('hidden');
  }

  if (medical3DScene) {
    medical3DScene.setTheme(isLightTheme);
  }

  setRangeState(currentRangeKey);
}

function toggleTheme() {
  setTheme(!isLightTheme);
}

function setRangeState(key) {
  const data = GLUCOSE_STATES[key];
  if (!data) return;
  currentRangeKey = key;

  const activeColor = isLightTheme ? data.lightColor : data.darkColor;
  const activeRgb = isLightTheme ? data.lightRgb : data.darkRgb;

  document.documentElement.style.setProperty('--theme-glow', activeColor);
  document.documentElement.style.setProperty('--theme-glow-rgb', activeRgb);

  if (medical3DScene) {
    medical3DScene.updateColors();
  }

  const heroVal = document.getElementById('hero-val');
  const heroTrend = document.getElementById('hero-trend');
  const heroTitle = document.getElementById('hero-title-state');
  const heroDelta = document.getElementById('hero-delta');
  const heroBadge = document.getElementById('hero-badge-range');

  if (heroVal) {
    heroVal.innerText = data.value;
    heroVal.style.color = activeColor;
  }
  if (heroTrend) {
    heroTrend.innerText = data.trend;
    heroTrend.style.color = activeColor;
  }
  if (heroTitle) {
    heroTitle.innerText = data.title;
    heroTitle.style.color = activeColor;
  }
  if (heroDelta) heroDelta.innerText = data.delta;
  if (heroBadge) {
    heroBadge.innerText = data.rangeLabel;
    heroBadge.style.borderColor = activeColor;
    heroBadge.style.color = activeColor;
  }

  document.querySelectorAll('.range-pill').forEach((btn) => {
    if (btn.getAttribute('data-range') === key) {
      btn.classList.add('active');
      btn.style.borderColor = activeColor;
      btn.style.color = activeColor;
    } else {
      btn.classList.remove('active');
      btn.style.borderColor = '';
      btn.style.color = '';
    }
  });

  const watchVal = document.getElementById('watch-val');
  const watchTrend = document.getElementById('watch-trend');
  if (watchVal) {
    watchVal.innerText = data.value;
    watchVal.style.color = activeColor;
  }
  if (watchTrend) {
    watchTrend.innerText = data.trend;
    watchTrend.style.color = activeColor;
  }

  const carVal = document.getElementById('car-val');
  const carTrend = document.getElementById('car-trend');
  const carStatus = document.getElementById('car-status');
  if (carVal) {
    carVal.innerText = data.value;
    carVal.style.color = activeColor;
  }
  if (carTrend) carTrend.innerText = data.trend;
  if (carStatus) {
    carStatus.innerText = data.title;
    carStatus.style.color = activeColor;
  }

  updateGraphPoint(data.value);
}

function create24hSeries() {
  const points = [];
  const now = new Date();
  const startOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0);

  let g = 112;
  for (let i = 0; i < 288; i++) {
    const d = new Date(startOfDay.getTime() + i * 5 * 60 * 1000);
    const hour = d.getHours() + d.getMinutes() / 60;

    let target = 115;
    if (hour >= 8 && hour <= 10) target = 142;
    if (hour >= 13.5 && hour <= 16) target = 158;
    if (hour >= 20.5 && hour <= 23) target = 136;
    if (hour >= 3 && hour <= 5) target = 92;

    g += (target - g) * 0.08 + (Math.random() - 0.5) * 5;
    g = Math.max(54, Math.min(265, g));

    points.push({
      timeStr: String(d.getHours()).padStart(2, '0') + ':' + String(d.getMinutes()).padStart(2, '0'),
      value: Math.round(g)
    });
  }
  return points;
}

const telemetry24h = create24hSeries();

function renderTelemetryGraph() {
  const svg = document.getElementById('graph-svg');
  if (!svg) return;

  const w = 800;
  const h = 240;
  const pL = 40;
  const pR = 20;
  const pT = 20;
  const pB = 30;

  const minG = 40;
  const maxG = 290;

  const getX = (i) => pL + (i / (telemetry24h.length - 1)) * (w - pL - pR);
  const getY = (val) => pT + (1 - (val - minG) / (maxG - minG)) * (h - pT - pB);

  let d = 'M ' + getX(0) + ' ' + getY(telemetry24h[0].value);
  for (let i = 1; i < telemetry24h.length; i++) {
    d += ' L ' + getX(i).toFixed(1) + ' ' + getY(telemetry24h[i].value).toFixed(1);
  }

  const path = document.getElementById('graph-path');
  const area = document.getElementById('graph-area');

  if (path) path.setAttribute('d', d);
  if (area) {
    const areaD = d + ' L ' + getX(telemetry24h.length - 1) + ' ' + (h - pB) + ' L ' + getX(0) + ' ' + (h - pB) + ' Z';
    area.setAttribute('d', areaD);
  }

  const overlay = document.getElementById('graph-overlay');
  const sLine = document.getElementById('s-line');
  const sDot = document.getElementById('s-dot');
  const tip = document.getElementById('s-tooltip');
  const tipTime = document.getElementById('s-time');
  const tipVal = document.getElementById('s-val');
  const tipStatus = document.getElementById('s-status');

  if (overlay) {
    const onScrub = (clientX) => {
      const rect = overlay.getBoundingClientRect();
      const pct = Math.max(0, Math.min(1, (clientX - rect.left) / rect.width));
      const idx = Math.round(pct * (telemetry24h.length - 1));
      const pt = telemetry24h[idx];

      const sx = getX(idx);
      const sy = getY(pt.value);

      if (sLine) {
        sLine.setAttribute('x1', sx);
        sLine.setAttribute('x2', sx);
        sLine.style.opacity = '1';
      }
      if (sDot) {
        sDot.setAttribute('cx', sx);
        sDot.setAttribute('cy', sy);
        sDot.style.opacity = '1';
      }
      if (tip) {
        tip.style.opacity = '1';
        tip.style.left = ((sx / w) * 100) + '%';
        tip.style.top = ((sy / h) * 100) + '%';
      }
      if (tipTime) tipTime.innerText = pt.timeStr;
      if (tipVal) tipVal.innerText = pt.value + ' mg/dL';
      if (tipStatus) {
        if (pt.value < 56) { tipStatus.innerText = 'Urgente Bajo'; tipStatus.style.color = isLightTheme ? '#b91c1c' : '#EF4444'; }
        else if (pt.value < 70) { tipStatus.innerText = 'Bajo'; tipStatus.style.color = isLightTheme ? '#dc2626' : '#F87171'; }
        else if (pt.value <= 180) { tipStatus.innerText = 'En Rango'; tipStatus.style.color = isLightTheme ? '#059669' : '#4ADE80'; }
        else if (pt.value <= 249) { tipStatus.innerText = 'Alto'; tipStatus.style.color = isLightTheme ? '#d97706' : '#FBBF24'; }
        else { tipStatus.innerText = 'Muy Alto'; tipStatus.style.color = isLightTheme ? '#ea580c' : '#FB923C'; }
      }
    };

    overlay.addEventListener('mousemove', (e) => onScrub(e.clientX));
    overlay.addEventListener('touchmove', (e) => {
      if (e.touches.length > 0) onScrub(e.touches[0].clientX);
    });
    overlay.addEventListener('mouseleave', () => {
      if (sLine) sLine.style.opacity = '0';
      if (sDot) sDot.style.opacity = '0';
      if (tip) tip.style.opacity = '0';
    });
  }
}

function updateGraphPoint(val) {
  if (telemetry24h.length > 0) {
    telemetry24h[telemetry24h.length - 1].value = val;
    renderTelemetryGraph();
  }
}

function setupCardTiltEffects() {
  document.querySelectorAll('.glass-card').forEach((card) => {
    card.addEventListener('mousemove', (e) => {
      const rect = card.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;
      const centerX = rect.width / 2;
      const centerY = rect.height / 2;

      const rotateX = ((y - centerY) / centerY) * -4;
      const rotateY = ((x - centerX) / centerX) * 4;

      card.style.transform = 'perspective(1000px) rotateX(' + rotateX.toFixed(1) + 'deg) rotateY(' + rotateY.toFixed(1) + 'deg) translateY(-2px)';
    });

    card.addEventListener('mouseleave', () => {
      card.style.transform = '';
    });
  });
}

document.addEventListener('DOMContentLoaded', () => {
  medical3DScene = new InteractiveMedical3DScene('canvas-3d-background');
  setTheme(false);
  renderTelemetryGraph();
  setupCardTiltEffects();

  const themeBtn = document.getElementById('btn-theme-toggle');
  if (themeBtn) {
    themeBtn.addEventListener('click', toggleTheme);
  }

  document.querySelectorAll('.range-pill').forEach((btn) => {
    btn.addEventListener('click', () => {
      const key = btn.getAttribute('data-range');
      setRangeState(key);
    });
  });
});
