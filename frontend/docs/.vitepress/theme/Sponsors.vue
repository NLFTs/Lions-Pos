<script setup>
const sponsors = [
  {
    name: 'NLFTs',
    img: 'https://www.nlfts.dev/nlfts.png',
    link: 'https://www.vuemastery.com/'
  },
  {
    name: 'Vehikl',
    img: 'https://sponsors.vuejs.org/images/vehikl.png',
    link: 'https://vehikl.com/'
  },
  {
    name: 'Vue.js Amsterdam',
    img: 'https://sponsors.vuejs.org/images/vue_js_amsterdam.png',
    link: 'https://vuejs.amsterdam/'
  },
  {
    name: 'Storyblok',
    img: 'https://sponsors.vuejs.org/images/storyblok.png',
    link: 'https://www.storyblok.com'
  },
  {
    name: 'Chrome',
    img: 'https://sponsors.vuejs.org/images/chrome_frameworks_fund.png',
    link: 'https://opencollective.com/2021-frameworks-fund'
  },
  {
    name: 'HeroDevs',
    img: 'https://sponsors.vuejs.org/images/herodevs.png',
    link: 'https://www.herodevs.com/'
  },
  {
    name: 'JavaScript Certification',
    img: 'https://sponsors.vuejs.org/images/javascript_certification.png',
    link: 'https://certificates.dev/javascript/'
  },
  {
    name: 'CodeRabbit',
    img: 'https://sponsors.vuejs.org/images/coderabbit_.png',
    link: 'https://www.coderabbit.ai/'
  },
  {
    name: 'ImageKit.io',
    img: 'https://sponsors.vuejs.org/images/imagekit_io.svg',
    link: 'https://imagekit.io/'
  },
  {
    name: 'Greptile',
    img: 'https://sponsors.vuejs.org/images/greptile.png',
    link: 'https://www.greptile.com/'
  },
  {
    name: 'SerpApi',
    img: 'https://sponsors.vuejs.org/images/serpapi.png',
    link: 'https://serpapi.com/'
  }
]
</script>

<template>
  <div class="sponsors-wrapper">
    <a href="mailto:sponsor@gaptek.docs" class="inquire-btn">
      Inquire about Special Sponsorship
    </a>
    
    <div class="sponsors-grid">
      <a 
        v-for="sponsor in sponsors" 
        :key="sponsor.name" 
        :href="sponsor.link" 
        target="_blank" 
        rel="noopener" 
        class="sponsor-item"
        :title="sponsor.name"
      >
        <img :src="sponsor.img" :alt="sponsor.name" class="sponsor-img" />
      </a>
      
      <a 
        href="https://github.com/sponsors/gaptek" 
        target="_blank" 
        rel="noopener" 
        class="sponsor-item become-sponsor"
      >
        <span>Become a Sponsor</span>
      </a>
    </div>
  </div>
</template>

<style scoped>
.sponsors-wrapper {
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.inquire-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 44px;
  border-radius: 6px;
  background-color: var(--vp-c-bg-soft);
  border: 1px solid var(--vp-c-divider);
  color: var(--vp-c-text-2);
  font-size: 11px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.2s ease;
  text-align: center;
}

.inquire-btn:hover {
  border-color: var(--vp-c-brand-1);
  color: var(--vp-c-text-1);
  background-color: var(--vp-c-bg-mute);
}

.sponsors-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1px;
  background-color: var(--vp-c-divider);
  border: 1px solid var(--vp-c-divider);
  border-radius: 6px;
  overflow: hidden;
}

.sponsor-item {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 64px;
  background-color: var(--vp-c-bg-soft);
  transition: all 0.2s ease;
  padding: 10px;
  text-decoration: none;
}

.sponsor-item:hover {
  background-color: var(--vp-c-bg-mute);
}

.sponsor-img {
  max-height: 32px;
  max-width: 85%;
  object-fit: contain;
  filter: grayscale(1) opacity(0.5);
  transition: all 0.2s ease;
}

.sponsor-item:hover .sponsor-img {
  filter: none;
  opacity: 1;
}

/* Specific handling for dark mode to make grayscale logos legible */
.dark .sponsor-img {
  filter: grayscale(1) brightness(1.5) opacity(0.6);
}

.dark .sponsor-item:hover .sponsor-img {
  filter: none;
  opacity: 1;
}

.become-sponsor {
  font-size: 11px;
  font-weight: 500;
  color: var(--vp-c-text-2);
  text-align: center;
}

.become-sponsor:hover {
  color: var(--vp-c-brand-1);
}
</style>
