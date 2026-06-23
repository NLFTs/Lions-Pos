<template>
  <div class="home-wrapper">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="container">
        <div class="hero-grid">
          <div class="hero-left">
            <h1 class="hero-heading">
              jawa Web<br />yang berdampak.
            </h1>
          </div>
          <div class="hero-right">
            <p class="hero-desc">
              Sistem aplikasi berbasis web yang dinamis untuk kebutuhan operasional spesifik,
              dasbor internal, sistem manajemen data, atau portal SaaS.
            </p>
            <div class="hero-actions">
              <a href="/login" class="btn-primary">
                <svg class="btn-icon" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
                  <polygon points="50,15 90,85 10,85" />
                </svg>
                <span>Mulai Keberangkatan</span>
              </a>
              <a href="docs/introduction" class="btn-secondary">Lihat Demo</a>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Infra Section -->
    <section class="infra-section">
      <div class="container infra-grid">
        <div class="infra-left">
          <div class="infra-badge">
            <span>⚙️</span> Infrastruktur Aplikasi
          </div>
          <h2 class="infra-heading">Kendali penuh atas arsitektur sistem Anda.</h2>
          <p class="infra-desc">
            Kombinasi fleksibel antara ketangguhan ekosistem backend modern dan kebebasan
            kerangka kerja frontend mutakhir. Menjamin skalabilitas alur kerja dari manajemen
            data hingga skalabilitas fungsional operasional.
          </p>
        </div>
        <div class="infra-right">
          <h3 class="infra-sub">
            <strong>Teknologi handal berstandar industri.</strong> Dibangun dengan struktur
            yang terukur demi kestabilan performa aplikasi sesuai kebutuhan bisnis Anda.
          </h3>

          <div class="tech-diagram">
            <!-- Left stack -->
            <div class="tech-stack">
              <!-- Spring Boot (Simple Icons official) -->
              <div class="tech-item">
                <svg class="tech-icon" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path fill="#6DB33F" d="M21.854 1.416a10.5 10.5 0 0 1-1.284 2.247A11.967 11.967 0 1 0 3.852 20.776l.444.395a11.954 11.954 0 0 0 19.632-8.297c.346-3.013-.568-6.865-2.074-11.458M5.58 20.875a1.017 1.017 0 1 1-.149-1.433a1.04 1.04 0 0 1 .149 1.432m16.199-3.581c-2.939 3.926-9.26 2.593-13.286 2.79c0 0-.716.05-1.432.148c0 0 .272-.123.618-.247c2.84-.987 4.173-1.185 5.901-2.074c3.235-1.654 6.47-5.284 7.112-9.038c-1.235 3.606-4.988 6.717-8.396 7.976c-2.346.865-6.568 1.704-6.568 1.704l-.173-.099c-2.865-1.407-2.963-7.63 2.272-9.63c2.296-.89 4.47-.395 6.963-.988c2.643-.617 5.705-2.593 6.94-5.186c1.382 4.174 3.061 10.643.049 14.644"/>
                </svg>
                <span>Arsitektur API</span>
              </div>
              <!-- Vue.js (Simple Icons official) -->
              <div class="tech-item">
                <svg class="tech-icon" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path fill="#4FC08D" d="M24 1.61h-9.94L12 5.16 9.94 1.61H0l12 20.78ZM12 14.08 5.16 2.23H9.59L12 6.41l2.41-4.18h4.43Z"/>
                </svg>
                <span>Frontend Modern</span>
              </div>
              <!-- GSAP / GreenSock (Simple Icons official) -->
              <div class="tech-item">
                <svg class="tech-icon" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path fill="#88CE02" d="M17.21 0c-.545.003-1.084.134-1.256.367c-.11.165-.192 1.196-.11 1.718c0 0 .032.345.09.614l-.02.182a7 7 0 0 1-.097.605c-.01.056-.207.095-.425.152l-.138-.042c-.234-.069-.385.123-.618.26c-.069-.04-.371-.178-.536-.082s-.275.193-.44.261c-.082-.041-.302-.041-.48.028a1.3 1.3 0 0 0-.483.278c-2.314.58-4.813 1.635-5.012 1.741c-1.017.522-2.679 1.415-3.434 2.033c-1.291 1.071-2.06 2.322-2.363 3.242c-.385 1.14-.275 1.827.096 1.387c.298-.366 1.632-1.454 2.475-1.999l-.002.007a3 3 0 0 1 .44-.26l.233-.124l.505-.323c.602.552.803 1.433.937 2.63c.22 1.841 1.704 2.693 3.434 2.72c1.8.028 2.446.399 3.119 1.305c.153.201.318.307.47.368a2 2 0 0 0-.16.405c-.075.17-.125.38-.157.608a.16.16 0 0 0-.03.075c-.068.536-.055 1.8-.068 2.473s-.028.77-.083.866c-.055.11-.11.178-.178.467c-.069.302-.193.384-.316.631c-.206.385-.165.81.041 1.003s.77.481 1.538.385c.77-.096.88-.151.756-.893c-.014-.11-.192-.605-.137-.797c.082-.206-.096-.563-.055-.577s.096-.288.096-.426c0-.137-.014-.796.137-1.14c.062-.14.193-.46.326-.785c.442-.723.459-1.161.48-1.41c.03-.202.046-.46.018-.744c.055-.083.289-.275.316-.646c0 0 .644-.337 1.102-1.148c.16.557.31.91.286 1.272c-.499.39-.684.678-.76.959c-.048-.02-.076-.037-.11-.04h-.027a.4.4 0 0 0-.106.029c-.192.068-.041 1.318.165 1.827s.316.81.398 1.36c.083.549-.192 1.222-.302 1.524c0 0-.179.536.233.824c.358.248 1.704.18 2.308.18c.605 0 1.511.219 2.088.109c.715-.124.824-.55.399-.77c-.426-.22-1.072-.329-1.91-.933c-.22-.152-.522-.289-.563-.412c-.041-.124-.041-.838-.027-1.457c.013-.618.22-1.414.288-1.84c.064-.398-.076-.388-.262-.351c.032-.147.066-.292.097-.446c.344-.632.193-1.223.193-1.223c.82-1.044.4-3.27.22-4.048c.64.303.96.188.96.188c.102-.055.192-.134.274-.224c.337-.362.51-.916.51-.916V11c.782-.783 1.151-1.936.26-2.692a1.33 1.33 0 0 0-.219-1.263a1.56 1.56 0 0 0-.37-1.731a1.4 1.4 0 0 0-.487-.297c-.2-.295-.245-.417-.572-.349c-.15-.165-.178-.288-.494-.178c0 0-.096-.234-.275-.289a.3.3 0 0 0-.05-.015c-.302-.21-.576-.215-.772-.16c-.064-.048-.061-.124-.07-.388c-.008-.2-.019-.486-.031-.744c.027-.328.102-.974.126-1.303c.028-.37.042-.948-.123-1.195C18.303.12 17.754-.003 17.21 0"/>
                </svg>
                <span>Animasi</span>
              </div>
              <!-- Pinia (Simple Icons official) -->
              <div class="tech-item">
                <svg class="tech-icon" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path fill="#FFD859" d="M13.277.002c-.079-.01-.193.024-.31.09a2.4 2.4 0 0 0-.415.311c-.301.274-.65.68-.985 1.176c-.672.99-1.31 2.356-1.485 3.785c-.12.977-.154 1.849-.074 2.578a5.9 5.9 0 0 1 1.956-.338c.922 0 1.804.218 2.618.613c.197-.637.34-1.395.445-2.246c.175-1.429-.1-2.907-.499-4.028a6.7 6.7 0 0 0-.653-1.378a2.3 2.3 0 0 0-.321-.401c-.097-.092-.199-.153-.277-.162m-6.81 2.965a.6.6 0 0 0-.288.117l-.013.011a.9.9 0 0 0-.177.327c-.051.15-.094.344-.126.565a8.2 8.2 0 0 0-.044 1.637c.08 1.222.397 2.665 1.079 3.769q.141.224.294.442c.726-.776 1.567-1.385 2.488-1.768c-.1-.79-.06-1.714.066-2.744c.038-.31.1-.616.176-.916a6.5 6.5 0 0 0-1.692-1.08a4.6 4.6 0 0 0-1.29-.366a1.8 1.8 0 0 0-.474.006zm11.751 1.789a5 5 0 0 0-.687.06a6 6 0 0 0-2.132.76a8 8 0 0 1-.036.437c-.11.888-.258 1.684-.475 2.364c.848.468 1.614 1.13 2.267 1.94c.823-.85 1.43-2.05 1.787-3.13c.187-.57.304-1.1.35-1.514c.022-.207.027-.38.017-.51c-.01-.133-.042-.206-.04-.202a.4.4 0 0 0-.114-.072a1.5 1.5 0 0 0-.375-.1a3.4 3.4 0 0 0-.562-.033m-6.254 3.139c-2.014 0-3.83 1.087-5.147 2.834c-1.316 1.748-2.131 4.16-2.131 6.807c0 2.65.806 4.258 2.131 5.218C8.132 23.707 9.96 24 11.964 24s3.831-.293 5.147-1.246c1.326-.96 2.133-2.568 2.133-5.218c0-2.647-.816-5.059-2.133-6.806c-1.317-1.748-3.134-2.835-5.147-2.835m-3.297 5.209a1.282 1.282 0 0 1 .292 2.518c.115.094.177.201.164.307c-.032.265-.521.424-1.09.354c-.571-.07-1.007-.342-.974-.607c.024-.198.302-.336.678-.364a1.282 1.282 0 0 1 .93-2.208m6.284.22a1.282 1.282 0 0 1 .726 2.303c.437.058.757.244.757.466c0 .267-.466.483-1.04.483s-1.04-.216-1.04-.483c0-.098.063-.189.17-.265a1.282 1.282 0 0 1 .427-2.505zm-6.318.215a.822.822 0 1 0 .028 1.645a.822.822 0 0 0-.028-1.645m6.284.22a.822.822 0 1 0 .027 1.644a.822.822 0 0 0-.027-1.644m-6.543.109a.338.338 0 1 1-.023.677a.338.338 0 0 1 .023-.677m6.284.22a.339.339 0 1 1-.018 0zm-3.91 1.179c.17.252.432.404.808.447c.372.043.694-.05.976-.28a.145.145 0 1 1 .183.226c-.344.28-.745.394-1.192.343c-.445-.051-.79-.24-1.016-.574a.145.145 0 1 1 .24-.162z"/>
                </svg>
                <span>Manajemen State</span>
              </div>
            </div>

            <!-- Center logo -->
            <div class="tech-center">
              <div class="tech-lines-wrap">
                <svg viewBox="0 0 160 240" fill="none" xmlns="http://www.w3.org/2000/svg" class="tech-lines-svg">
                  <path d="M 0 30 C 60 30, 70 120, 130 120"  stroke="#6DB33F" stroke-width="1.5" opacity="0.6"/>
                  <path d="M 0 90 C 60 90, 70 120, 130 120"  stroke="#4FC08D" stroke-width="1.5" opacity="0.6"/>
                  <path d="M 0 150 C 60 150, 70 120, 130 120" stroke="#88CE02" stroke-width="1.5" opacity="0.6"/>
                  <path d="M 0 210 C 60 210, 70 120, 130 120" stroke="#FFD859" stroke-width="1.5" opacity="0.6"/>
                  <line x1="130" y1="120" x2="160" y2="120" stroke="var(--vp-c-divider)" stroke-width="1.5" stroke-dasharray="4 4"/>
                </svg>
              </div>
              <div class="tech-logo">
                <img :src="withBase('/logo-gaptek.svg')" alt="Gaptek Logo" class="tech-logo-img" />
              </div>
            </div>

            <!-- Right mockup -->
            <div class="tech-mockup">
              <div class="mockup-dots">
                <span class="dot dot-red"></span>
                <span class="dot dot-yellow"></span>
                <span class="dot dot-green"></span>
              </div>
              <div class="mockup-line"></div>
              <div class="mockup-box"></div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="cta-section">
      <div class="container">
        <div class="cta-card">
          <div class="cta-left">
            <h2 class="cta-heading">
              Siap membangun sistem?
              <span class="cta-sub">
                Mulai iterasi proyek Anda sekarang. Hubungi tim kami untuk mendiskusikan
                kebutuhan infrastruktur skala
                <span class="cta-pro">Pro</span> atau tingkat
                <span class="cta-enterprise">Enterprise</span> perusahaan Anda.
              </span>
            </h2>
            <div class="cta-actions">
              <a href="/introduction" class="btn-dark">Hubungi Kami</a>
              <a href="/introduction" class="btn-outline">Konsultasikan Ide</a>
            </div>
          </div>
          <div class="cta-right">
            <p class="cta-desc">
              <strong>Jelajahi Opsi Enterprise</strong> dengan jaminan performa kustom,
              isolasi pipeline data, serta dukungan Service Level Agreement (SLA) khusus.
            </p>
            <a href="/introduction" class="btn-ghost">Solusi Korporat</a>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { withBase } from 'vitepress'
</script>

<style scoped>
/* ── Base ── */
.home-wrapper {
  min-height: 100vh;
  background: var(--vp-c-bg);
  color: var(--vp-c-text-1);
  font-family: var(--vp-font-family-base);
  -webkit-font-smoothing: antialiased;
}

.container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 2rem;
}

/* ── Hero ── */
.hero-section {
  padding: 8rem 2rem 3rem;
}

.hero-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 2rem;
}

@media (min-width: 1024px) {
  .hero-grid {
    grid-template-columns: 7fr 5fr;
    gap: 4rem;
    align-items: start;
  }
}

.hero-heading {
  font-size: clamp(2.75rem, 6vw, 4rem);
  font-weight: 700;
  letter-spacing: -0.02em;
  line-height: 1.02;
  color: var(--vp-c-text-1);
  border: none !important;
  margin: 0;
  padding: 0;
}

.hero-right {
  padding-top: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.hero-desc {
  font-size: clamp(0.9375rem, 1.5vw, 1.0625rem);
  line-height: 1.75;
  color: var(--vp-c-text-2);
  max-width: 28rem;
  margin: 0;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.75rem;
}

/* ── Buttons ── */
.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  height: 2.75rem;
  padding: 0 1.25rem;
  border-radius: 9999px;
  background: var(--vp-c-text-1);
  color: var(--vp-c-bg);
  font-size: 0.875rem;
  font-weight: 500;
  text-decoration: none;
  transition: opacity 0.2s;
  border: none;
}

.btn-primary:hover { opacity: 0.85; }

.btn-icon {
  width: 0.75rem;
  height: 0.75rem;
  fill: currentColor;
}

.btn-secondary {
  display: inline-flex;
  align-items: center;
  height: 2.75rem;
  padding: 0 1.25rem;
  border-radius: 9999px;
  border: 1px solid var(--vp-c-divider);
  background: var(--vp-c-bg-soft);
  color: var(--vp-c-text-1);
  font-size: 0.875rem;
  font-weight: 500;
  text-decoration: none;
  transition: background 0.2s;
}

.btn-secondary:hover { background: var(--vp-c-bg-mute); }

/* ── Infra ── */
.infra-section {
  border-bottom: 1px solid var(--vp-c-divider);
}

.infra-grid {
  display: grid;
  grid-template-columns: 1fr;
  border-bottom: 1px solid var(--vp-c-divider);
}

@media (min-width: 1024px) {
  .infra-grid {
    grid-template-columns: 4fr 8fr;
    border-bottom: none;
  }
}

.infra-left {
  padding: 3rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 1rem;
  border-bottom: 1px solid var(--vp-c-divider);
}

@media (min-width: 1024px) {
  .infra-left {
    border-bottom: none;
    border-right: 1px solid var(--vp-c-divider);
    padding: 4rem 3rem;
  }
}

.infra-badge {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.75rem;
  font-family: monospace;
  color: var(--vp-c-text-3);
}

.infra-heading {
  font-size: clamp(1.75rem, 3vw, 2.25rem);
  font-weight: 600;
  letter-spacing: -0.02em;
  line-height: 1.25;
  color: var(--vp-c-text-1);
  border: none !important;
  margin: 0;
}

.infra-desc {
  font-size: 0.875rem;
  line-height: 1.75;
  color: var(--vp-c-text-2);
  margin: 0;
}

.infra-right {
  padding: 3rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 2rem;
  background: var(--vp-c-bg-soft);
}

@media (min-width: 1024px) {
  .infra-right { padding: 4rem 3rem; }
}

.infra-sub {
  font-size: 1.125rem;
  font-weight: 400;
  color: var(--vp-c-text-2);
  line-height: 1.6;
  border: none !important;
  margin: 0;
}

.infra-sub strong {
  color: var(--vp-c-text-1);
  font-weight: 500;
}

/* ── Tech Diagram ── */
.tech-diagram {
  display: flex;
  align-items: center;
  gap: 0;
  padding-top: 1.5rem;
  height: 240px;
  position: relative;
}

.tech-stack {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
  flex: 0 0 auto;
  width: 11rem;
  z-index: 2;
}

.tech-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  border: 1px solid var(--vp-c-divider);
  background: var(--vp-c-bg);
  border-radius: 0.5rem;
  padding: 0 1rem;
  height: 2.75rem;
  font-size: 0.75rem;
  font-family: monospace;
  color: var(--vp-c-text-1);
}

.tech-icon {
  width: 1.125rem;
  height: 1.125rem;
  flex-shrink: 0;
}

.tech-center {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 1.5rem;
  position: relative;
  height: 100%;
}

.tech-lines-wrap {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.tech-lines-svg {
  width: 100%;
  height: 100%;
}

.tech-logo {
  width: 3.5rem;
  height: 3.5rem;
  border-radius: 0.75rem;
  border: 1px solid var(--vp-c-divider);
  background: var(--vp-c-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: 0.375rem;
  box-shadow: 0 4px 24px rgba(0,0,0,0.12);
  z-index: 2;
  position: relative;
}

.tech-logo-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.tech-logo-svg {
  width: 100%;
  height: 100%;
}

.logo-bg {
  color: var(--vp-c-text-1);
}

.tech-mockup {
  flex: 0 0 auto;
  width: 11rem;
  border-radius: 0.75rem;
  border: 1px solid var(--vp-c-divider);
  background: var(--vp-c-bg-soft);
  padding: 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.mockup-dots {
  display: flex;
  gap: 0.25rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid var(--vp-c-divider);
}

.dot {
  width: 0.375rem;
  height: 0.375rem;
  border-radius: 50%;
}

.dot-red    { background: #FF5F56; }
.dot-yellow { background: #FFBD2E; }
.dot-green  { background: #27C93F; }

.mockup-line {
  height: 0.5rem;
  width: 75%;
  border-radius: 9999px;
  background: var(--vp-c-divider);
}

.mockup-box {
  height: 2.5rem;
  width: 100%;
  border-radius: 0.375rem;
  border: 1px dashed var(--vp-c-divider);
  background: var(--vp-c-bg);
}

/* ── CTA ── */
.cta-section {
  padding: 6rem 2rem;
  position: relative;
}

.cta-card {
  display: grid;
  grid-template-columns: 1fr;
  gap: 2rem;
  border: 1px solid var(--vp-c-divider);
  border-radius: 1rem;
  padding: 2.5rem;
  background: linear-gradient(to bottom, var(--vp-c-bg-soft), var(--vp-c-bg));
}

@media (min-width: 1024px) {
  .cta-card {
    grid-template-columns: 8fr 4fr;
    gap: 0;
  }
}

.cta-left {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.cta-heading {
  font-size: clamp(1.5rem, 3vw, 2rem);
  font-weight: 600;
  letter-spacing: -0.02em;
  line-height: 1.35;
  color: var(--vp-c-text-1);
  max-width: 42rem;
  border: none !important;
  margin: 0;
}

.cta-sub {
  font-size: inherit;
  font-weight: 400;
  color: var(--vp-c-text-2);
}

.cta-pro        { color: #0070F3; }
.cta-enterprise { color: #7928CA; }

.cta-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.btn-dark {
  display: inline-flex;
  align-items: center;
  height: 2.75rem;
  padding: 0 1.5rem;
  border-radius: 0.375rem;
  background: var(--vp-c-text-1);
  color: var(--vp-c-bg);
  font-size: 0.875rem;
  font-weight: 500;
  text-decoration: none;
  transition: opacity 0.2s;
  border: none;
}

.btn-dark:hover { opacity: 0.85; }

.btn-outline {
  display: inline-flex;
  align-items: center;
  height: 2.75rem;
  padding: 0 1.5rem;
  border-radius: 0.375rem;
  border: 1px solid var(--vp-c-divider);
  background: transparent;
  color: var(--vp-c-text-2);
  font-size: 0.875rem;
  font-weight: 500;
  text-decoration: none;
  transition: color 0.2s, border-color 0.2s;
}

.btn-outline:hover {
  color: var(--vp-c-text-1);
  border-color: var(--vp-c-text-2);
}

.cta-right {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 1rem;
  padding-top: 2rem;
  border-top: 1px solid var(--vp-c-divider);
}

@media (min-width: 1024px) {
  .cta-right {
    padding-top: 0;
    padding-left: 2.5rem;
    border-top: none;
    border-left: 1px solid var(--vp-c-divider);
  }
}

.cta-desc {
  font-size: 0.875rem;
  line-height: 1.75;
  color: var(--vp-c-text-2);
  margin: 0;
}

.cta-desc strong {
  color: var(--vp-c-text-1);
  font-weight: 500;
}

.btn-ghost {
  display: inline-flex;
  align-items: center;
  height: 2.75rem;
  padding: 0 1.5rem;
  border-radius: 0.375rem;
  border: 1px solid var(--vp-c-divider);
  background: var(--vp-c-bg-soft);
  color: var(--vp-c-text-1);
  font-size: 0.875rem;
  font-weight: 500;
  text-decoration: none;
  transition: background 0.2s;
  width: fit-content;
}

.btn-ghost:hover { background: var(--vp-c-bg-mute); }
</style>
