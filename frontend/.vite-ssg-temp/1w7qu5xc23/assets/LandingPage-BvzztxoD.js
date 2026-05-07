import { n as useAuthStore } from "../main.mjs";
import { n as Navbar_default, r as LandingLayout_default, t as GradientBlinds_default } from "./GradientBlinds-N6f08zFD.js";
import { t as _plugin_vue_export_helper_default } from "./_plugin-vue_export-helper-DMwexRDj.js";
import { t as _sfc_main$1 } from "./Button-Bj0EF1Kv.js";
import { Transition, createBlock, createCommentVNode, createTextVNode, createVNode, onMounted, onUnmounted, openBlock, ref, unref, useSSRContext, withCtx } from "vue";
import { useRouter } from "vue-router";
import { storeToRefs } from "pinia";
import { ssrRenderComponent, ssrRenderStyle } from "vue/server-renderer";
import { Zap } from "lucide-vue-next";
import gsap from "gsap";
import { ScrollTrigger } from "gsap/ScrollTrigger";
import Lenis from "lenis";
//#region src/lib/gsap.js
gsap.registerPlugin(ScrollTrigger);
gsap.config({ nullTargetWarn: false });
//#endregion
//#region src/lib/lenis.js
var lenisInstance = null;
var tickerFunction = null;
var initLenis = () => {
	if (typeof window === "undefined") return;
	lenisInstance = new Lenis({
		duration: 1.2,
		easing: (t) => Math.min(1, 1.001 - Math.pow(2, -10 * t)),
		orientation: "vertical",
		gestureOrientation: "vertical",
		smoothWheel: true,
		wheelMultiplier: 1,
		smoothTouch: false,
		touchMultiplier: 2,
		infinite: false
	});
	lenisInstance.on("scroll", ScrollTrigger.update);
	if (tickerFunction) gsap.ticker.remove(tickerFunction);
	tickerFunction = (time) => {
		lenisInstance?.raf(time * 1e3);
	};
	gsap.ticker.add(tickerFunction);
	gsap.ticker.lagSmoothing(0);
	return lenisInstance;
};
var destroyLenis = () => {
	if (tickerFunction) {
		gsap.ticker.remove(tickerFunction);
		tickerFunction = null;
	}
	if (lenisInstance) {
		lenisInstance.destroy();
		lenisInstance = null;
	}
};
//#endregion
//#region src/hooks/useGsap.js
function useGsap(callback) {
	onMounted(() => {
		initLenis();
		if (callback && typeof callback === "function") callback(gsap, ScrollTrigger);
		ScrollTrigger.refresh();
	});
	onUnmounted(() => {
		ScrollTrigger.getAll().forEach((trigger) => trigger.kill());
		gsap.killTweensOf("*");
		destroyLenis();
	});
}
//#endregion
//#region src/pages/LandingPage.vue
var brandName = "gaptek";
var _sfc_main = {
	__name: "LandingPage",
	__ssrInlineRender: true,
	setup(__props) {
		const router = useRouter();
		const { isAuthenticated } = storeToRefs(useAuthStore());
		const showCookieBanner = ref(false);
		onMounted(() => {
			if (!localStorage.getItem("gaptek_cookies_accepted")) showCookieBanner.value = true;
		});
		const hideCookieBanner = () => {
			localStorage.setItem("gaptek_cookies_accepted", "true");
			showCookieBanner.value = false;
		};
		useGsap((gsap, ScrollTrigger) => {});
		const navigationItems = [
			{
				name: "Layanan Kami",
				path: "#offer",
				hasDropdown: true
			},
			{
				name: "Untuk Siapa",
				path: "#target",
				hasDropdown: true
			},
			{
				name: "Harga",
				path: "#pricing"
			},
			{
				name: "Tentang Kami",
				path: "/about"
			}
		];
		const handleNavigation = (path) => {
			if (path.startsWith("#")) {
				const el = document.querySelector(path);
				if (el) el.scrollIntoView({ behavior: "smooth" });
			} else router.push(path);
		};
		const navigateToLogin = () => {
			router.push("/login");
		};
		return (_ctx, _push, _parent, _attrs) => {
			_push(ssrRenderComponent(LandingLayout_default, _attrs, {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) {
						_push(ssrRenderComponent(Navbar_default, {
							"brand-name": brandName,
							"nav-items": navigationItems,
							onNavigate: handleNavigation
						}, {
							logo: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) {
									_push(`<div class="h-full w-full bg-zinc-100 flex items-center justify-center" data-v-2022fe93${_scopeId}>`);
									_push(ssrRenderComponent(unref(Zap), { class: "h-4 w-4 text-zinc-900" }, null, _parent, _scopeId));
									_push(`</div>`);
								} else return [createVNode("div", { class: "h-full w-full bg-zinc-100 flex items-center justify-center" }, [createVNode(unref(Zap), { class: "h-4 w-4 text-zinc-900" })])];
							}),
							actions: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) {
									_push(ssrRenderComponent(_sfc_main$1, {
										variant: "ghost",
										class: "hidden sm:inline-flex font-bold text-zinc-300 hover:text-white transition-colors duration-300",
										onClick: navigateToLogin
									}, {
										default: withCtx((_, _push, _parent, _scopeId) => {
											if (_push) _push(` Masuk `);
											else return [createTextVNode(" Masuk ")];
										}),
										_: 1
									}, _parent, _scopeId));
									_push(ssrRenderComponent(_sfc_main$1, {
										class: "font-bold px-3 sm:px-6 py-1.5 sm:py-2 text-xs sm:text-base rounded-xl bg-zinc-100 text-zinc-900 hover:bg-white transition-all active:scale-95",
										onClick: navigateToLogin
									}, {
										default: withCtx((_, _push, _parent, _scopeId) => {
											if (_push) _push(` Mulai Gratis `);
											else return [createTextVNode(" Mulai Gratis ")];
										}),
										_: 1
									}, _parent, _scopeId));
								} else return [createVNode(_sfc_main$1, {
									variant: "ghost",
									class: "hidden sm:inline-flex font-bold text-zinc-300 hover:text-white transition-colors duration-300",
									onClick: navigateToLogin
								}, {
									default: withCtx(() => [createTextVNode(" Masuk ")]),
									_: 1
								}), createVNode(_sfc_main$1, {
									class: "font-bold px-3 sm:px-6 py-1.5 sm:py-2 text-xs sm:text-base rounded-xl bg-zinc-100 text-zinc-900 hover:bg-white transition-all active:scale-95",
									onClick: navigateToLogin
								}, {
									default: withCtx(() => [createTextVNode(" Mulai Gratis ")]),
									_: 1
								})];
							}),
							_: 1
						}, _parent, _scopeId));
						_push(`<main class="relative flex-grow flex flex-col items-center pt-24 pb-32 px-6 overflow-hidden" data-v-2022fe93${_scopeId}><div class="absolute inset-0 z-0 opacity-100 pointer-events-none" data-v-2022fe93${_scopeId}>`);
						_push(ssrRenderComponent(GradientBlinds_default, {
							"gradient-colors": ["#1EA03F", "#182FFF"],
							angle: 0,
							noise: .3,
							"blind-count": 12,
							"blind-min-width": 50,
							"spotlight-radius": .5,
							"spotlight-softness": 1,
							"spotlight-opacity": 1,
							"mouse-dampening": .15,
							"distort-amount": 0,
							"shine-direction": "left",
							"mix-blend-mode": "screen"
						}, null, _parent, _scopeId));
						_push(`</div><div class="relative z-10 space-y-10 max-w-5xl mx-auto text-center" data-v-2022fe93${_scopeId}><h1 class="text-6xl md:text-8xl font-medium font-serif tracking-tight text-white leading-[1.05]" data-v-2022fe93${_scopeId}> Platform All-in-One<br data-v-2022fe93${_scopeId}> untuk <span class="italic text-primary" data-v-2022fe93${_scopeId}>Retail &amp; F&amp;B </span> modern </h1><p class="text-lg md:text-xl text-zinc-400 font-medium max-w-2xl mx-auto leading-relaxed" data-v-2022fe93${_scopeId}> Manajemen proyek bertenaga AI yang membantu Anda <br class="hidden md:block" data-v-2022fe93${_scopeId}> membangun aplikasi impian lebih cepat dan terorganisir. </p><div class="flex flex-col sm:flex-row items-center justify-center gap-4 pt-4" data-v-2022fe93${_scopeId}>`);
						_push(ssrRenderComponent(_sfc_main$1, {
							class: "h-12 px-8 rounded-full bg-white text-zinc-900 hover:scale-105 transition-transform duration-300 font-medium",
							onClick: navigateToLogin
						}, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) _push(` Mulai Gratis Sekarang `);
								else return [createTextVNode(" Mulai Gratis Sekarang ")];
							}),
							_: 1
						}, _parent, _scopeId));
						_push(ssrRenderComponent(_sfc_main$1, {
							variant: "ghost",
							class: "h-12 px-8 rounded-full border border-zinc-800 text-white hover:bg-zinc-900 transition-colors duration-300"
						}, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) _push(` Lihat Demo `);
								else return [createTextVNode(" Lihat Demo ")];
							}),
							_: 1
						}, _parent, _scopeId));
						_push(`</div></div><div class="relative mt-24 w-full max-w-7xl mx-auto px-4 md:px-0" data-v-2022fe93${_scopeId}><div class="absolute -inset-4 bg-primary/10 blur-3xl rounded-[3rem] pointer-events-none" data-v-2022fe93${_scopeId}></div><div class="relative bg-zinc-950 border border-white/10 rounded-3xl shadow-[0_32px_64px_-16px_rgba(0,0,0,0.8)] aspect-[16/10] overflow-hidden flex flex-col overscroll-contain" data-v-2022fe93${_scopeId}><div class="flex-grow relative bg-[#09090b] overflow-hidden overscroll-none" data-v-2022fe93${_scopeId}><iframe src="/dashboard" class="absolute top-0 left-0 w-[117.6%] h-[117.6%] border-none origin-top-left scale-[0.85] will-change-transform" style="${ssrRenderStyle({ "scrollbar-width": "none" })}" scrolling="no" data-v-2022fe93${_scopeId}></iframe></div></div></div></main>`);
						if (showCookieBanner.value) _push(`<div class="fixed bottom-8 right-8 z-[60] max-w-sm" data-v-2022fe93${_scopeId}><div class="bg-white text-zinc-900 p-5 rounded-2xl shadow-2xl flex items-center justify-between gap-8 backdrop-blur-xl bg-opacity-95 border border-black/5" data-v-2022fe93${_scopeId}><div class="space-y-1" data-v-2022fe93${_scopeId}><p class="text-[11px] leading-relaxed font-semibold" data-v-2022fe93${_scopeId}>Kebijakan Cookie</p><p class="text-[10px] leading-relaxed opacity-70" data-v-2022fe93${_scopeId}> Kami menggunakan cookie untuk mempersonalisasi konten, menjalankan iklan, dan menganalisis lalu lintas. </p></div><button class="shrink-0 px-5 py-2 bg-zinc-900 text-white rounded-xl text-[10px] font-bold hover:bg-black transition-all active:scale-95 shadow-lg shadow-black/10" data-v-2022fe93${_scopeId}> Oke </button></div></div>`);
						else _push(`<!---->`);
					} else return [
						createVNode(Navbar_default, {
							"brand-name": brandName,
							"nav-items": navigationItems,
							onNavigate: handleNavigation
						}, {
							logo: withCtx(() => [createVNode("div", { class: "h-full w-full bg-zinc-100 flex items-center justify-center" }, [createVNode(unref(Zap), { class: "h-4 w-4 text-zinc-900" })])]),
							actions: withCtx(() => [createVNode(_sfc_main$1, {
								variant: "ghost",
								class: "hidden sm:inline-flex font-bold text-zinc-300 hover:text-white transition-colors duration-300",
								onClick: navigateToLogin
							}, {
								default: withCtx(() => [createTextVNode(" Masuk ")]),
								_: 1
							}), createVNode(_sfc_main$1, {
								class: "font-bold px-3 sm:px-6 py-1.5 sm:py-2 text-xs sm:text-base rounded-xl bg-zinc-100 text-zinc-900 hover:bg-white transition-all active:scale-95",
								onClick: navigateToLogin
							}, {
								default: withCtx(() => [createTextVNode(" Mulai Gratis ")]),
								_: 1
							})]),
							_: 1
						}),
						createVNode("main", { class: "relative flex-grow flex flex-col items-center pt-24 pb-32 px-6 overflow-hidden" }, [
							createVNode("div", { class: "absolute inset-0 z-0 opacity-100 pointer-events-none" }, [createVNode(GradientBlinds_default, {
								"gradient-colors": ["#1EA03F", "#182FFF"],
								angle: 0,
								noise: .3,
								"blind-count": 12,
								"blind-min-width": 50,
								"spotlight-radius": .5,
								"spotlight-softness": 1,
								"spotlight-opacity": 1,
								"mouse-dampening": .15,
								"distort-amount": 0,
								"shine-direction": "left",
								"mix-blend-mode": "screen"
							})]),
							createVNode("div", { class: "relative z-10 space-y-10 max-w-5xl mx-auto text-center" }, [
								createVNode("h1", { class: "text-6xl md:text-8xl font-medium font-serif tracking-tight text-white leading-[1.05]" }, [
									createTextVNode(" Platform All-in-One"),
									createVNode("br"),
									createTextVNode(" untuk "),
									createVNode("span", { class: "italic text-primary" }, "Retail & F&B "),
									createTextVNode(" modern ")
								]),
								createVNode("p", { class: "text-lg md:text-xl text-zinc-400 font-medium max-w-2xl mx-auto leading-relaxed" }, [
									createTextVNode(" Manajemen proyek bertenaga AI yang membantu Anda "),
									createVNode("br", { class: "hidden md:block" }),
									createTextVNode(" membangun aplikasi impian lebih cepat dan terorganisir. ")
								]),
								createVNode("div", { class: "flex flex-col sm:flex-row items-center justify-center gap-4 pt-4" }, [createVNode(_sfc_main$1, {
									class: "h-12 px-8 rounded-full bg-white text-zinc-900 hover:scale-105 transition-transform duration-300 font-medium",
									onClick: navigateToLogin
								}, {
									default: withCtx(() => [createTextVNode(" Mulai Gratis Sekarang ")]),
									_: 1
								}), createVNode(_sfc_main$1, {
									variant: "ghost",
									class: "h-12 px-8 rounded-full border border-zinc-800 text-white hover:bg-zinc-900 transition-colors duration-300"
								}, {
									default: withCtx(() => [createTextVNode(" Lihat Demo ")]),
									_: 1
								})])
							]),
							createVNode("div", { class: "relative mt-24 w-full max-w-7xl mx-auto px-4 md:px-0" }, [createVNode("div", { class: "absolute -inset-4 bg-primary/10 blur-3xl rounded-[3rem] pointer-events-none" }), createVNode("div", { class: "relative bg-zinc-950 border border-white/10 rounded-3xl shadow-[0_32px_64px_-16px_rgba(0,0,0,0.8)] aspect-[16/10] overflow-hidden flex flex-col overscroll-contain" }, [createVNode("div", { class: "flex-grow relative bg-[#09090b] overflow-hidden overscroll-none" }, [createVNode("iframe", {
								src: "/dashboard",
								class: "absolute top-0 left-0 w-[117.6%] h-[117.6%] border-none origin-top-left scale-[0.85] will-change-transform",
								style: { "scrollbar-width": "none" },
								scrolling: "no"
							})])])])
						]),
						createVNode(Transition, {
							"enter-active-class": "transition duration-500 ease-out",
							"enter-from-class": "transform translate-y-20 opacity-0",
							"enter-to-class": "transform translate-y-0 opacity-100",
							"leave-active-class": "transition duration-300 ease-in",
							"leave-from-class": "transform translate-y-0 opacity-100",
							"leave-to-class": "transform translate-y-20 opacity-0"
						}, {
							default: withCtx(() => [showCookieBanner.value ? (openBlock(), createBlock("div", {
								key: 0,
								class: "fixed bottom-8 right-8 z-[60] max-w-sm"
							}, [createVNode("div", { class: "bg-white text-zinc-900 p-5 rounded-2xl shadow-2xl flex items-center justify-between gap-8 backdrop-blur-xl bg-opacity-95 border border-black/5" }, [createVNode("div", { class: "space-y-1" }, [createVNode("p", { class: "text-[11px] leading-relaxed font-semibold" }, "Kebijakan Cookie"), createVNode("p", { class: "text-[10px] leading-relaxed opacity-70" }, " Kami menggunakan cookie untuk mempersonalisasi konten, menjalankan iklan, dan menganalisis lalu lintas. ")]), createVNode("button", {
								onClick: hideCookieBanner,
								class: "shrink-0 px-5 py-2 bg-zinc-900 text-white rounded-xl text-[10px] font-bold hover:bg-black transition-all active:scale-95 shadow-lg shadow-black/10"
							}, " Oke ")])])) : createCommentVNode("", true)]),
							_: 1
						})
					];
				}),
				_: 1
			}, _parent));
		};
	}
};
var _sfc_setup = _sfc_main.setup;
_sfc_main.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/pages/LandingPage.vue");
	return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
var LandingPage_default = /* @__PURE__ */ _plugin_vue_export_helper_default(_sfc_main, [["__scopeId", "data-v-2022fe93"]]);
//#endregion
export { LandingPage_default as default };
