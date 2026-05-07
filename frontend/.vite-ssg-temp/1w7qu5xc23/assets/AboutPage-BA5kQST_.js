import { n as Navbar_default, r as LandingLayout_default, t as GradientBlinds_default } from "./GradientBlinds-N6f08zFD.js";
import { t as _sfc_main$1 } from "./Button-Bj0EF1Kv.js";
import { createTextVNode, createVNode, unref, useSSRContext, withCtx } from "vue";
import { useRouter } from "vue-router";
import { ssrRenderComponent } from "vue/server-renderer";
import { ArrowLeft, Heart, Rocket, Shield, Sparkles, Trophy, Users, Zap } from "lucide-vue-next";
//#region src/pages/AboutPage.vue
var _sfc_main = {
	__name: "AboutPage",
	__ssrInlineRender: true,
	setup(__props) {
		const router = useRouter();
		const navigationItems = [
			{
				name: "Layanan Kami",
				path: "/#offer",
				hasDropdown: true
			},
			{
				name: "Untuk Siapa",
				path: "/#target",
				hasDropdown: true
			},
			{
				name: "Harga",
				path: "/#pricing"
			},
			{
				name: "Tentang Kami",
				path: "/about"
			}
		];
		const handleNavigation = (path) => {
			if (path.startsWith("/#")) router.push("/").then(() => {
				setTimeout(() => {
					const el = document.querySelector(path.substring(1));
					if (el) el.scrollIntoView({ behavior: "smooth" });
				}, 100);
			});
			else router.push(path);
		};
		const navigateBack = () => {
			router.back();
		};
		return (_ctx, _push, _parent, _attrs) => {
			_push(ssrRenderComponent(LandingLayout_default, _attrs, {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) {
						_push(ssrRenderComponent(Navbar_default, {
							"brand-name": "gaptek",
							"nav-items": navigationItems,
							onNavigate: handleNavigation
						}, {
							logo: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) {
									_push(`<div class="h-full w-full bg-zinc-100 flex items-center justify-center"${_scopeId}>`);
									_push(ssrRenderComponent(unref(Zap), { class: "h-4 w-4 text-zinc-900" }, null, _parent, _scopeId));
									_push(`</div>`);
								} else return [createVNode("div", { class: "h-full w-full bg-zinc-100 flex items-center justify-center" }, [createVNode(unref(Zap), { class: "h-4 w-4 text-zinc-900" })])];
							}),
							actions: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) {
									_push(ssrRenderComponent(_sfc_main$1, {
										variant: "ghost",
										class: "hidden sm:inline-flex font-bold text-zinc-300 hover:text-white transition-colors duration-300",
										onClick: ($event) => unref(router).push("/login")
									}, {
										default: withCtx((_, _push, _parent, _scopeId) => {
											if (_push) _push(` Masuk `);
											else return [createTextVNode(" Masuk ")];
										}),
										_: 1
									}, _parent, _scopeId));
									_push(ssrRenderComponent(_sfc_main$1, {
										class: "font-bold px-6 py-2 rounded-xl bg-zinc-100 text-zinc-900 hover:bg-white transition-all active:scale-95",
										onClick: ($event) => unref(router).push("/login")
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
									onClick: ($event) => unref(router).push("/login")
								}, {
									default: withCtx(() => [createTextVNode(" Masuk ")]),
									_: 1
								}, 8, ["onClick"]), createVNode(_sfc_main$1, {
									class: "font-bold px-6 py-2 rounded-xl bg-zinc-100 text-zinc-900 hover:bg-white transition-all active:scale-95",
									onClick: ($event) => unref(router).push("/login")
								}, {
									default: withCtx(() => [createTextVNode(" Mulai Gratis ")]),
									_: 1
								}, 8, ["onClick"])];
							}),
							_: 1
						}, _parent, _scopeId));
						_push(`<main class="relative flex-grow flex flex-col items-center pt-32 pb-24 px-6 overflow-hidden"${_scopeId}><div class="absolute inset-0 z-0 opacity-40 pointer-events-none"${_scopeId}>`);
						_push(ssrRenderComponent(GradientBlinds_default, {
							"gradient-colors": ["#1EA03F", "#182FFF"],
							angle: 0,
							noise: .2,
							"blind-count": 10,
							"mix-blend-mode": "screen"
						}, null, _parent, _scopeId));
						_push(`</div><div class="relative z-10 max-w-4xl mx-auto space-y-24"${_scopeId}><div class="text-center space-y-8"${_scopeId}><button class="inline-flex items-center gap-2 text-sm font-medium text-zinc-500 hover:text-white transition-colors mb-4 group"${_scopeId}>`);
						_push(ssrRenderComponent(unref(ArrowLeft), { class: "w-4 h-4 group-hover:-translate-x-1 transition-transform" }, null, _parent, _scopeId));
						_push(` Kembali </button><h1 class="text-5xl md:text-7xl font-serif italic tracking-tight text-white leading-tight"${_scopeId}> Kami Membangun <span class="text-primary not-italic"${_scopeId}>Masa Depan</span> Retail. </h1><p class="text-xl text-zinc-400 font-medium max-w-2xl mx-auto leading-relaxed"${_scopeId}> Gaptek hadir sebagai solusi revolusioner untuk membantu pengusaha retail dan F&amp;B naik kelas melalui teknologi otomasi dan kecerdasan data yang elegan. </p></div><div class="grid grid-cols-1 md:grid-cols-2 gap-12 items-center"${_scopeId}><div class="space-y-6"${_scopeId}><div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-primary/10 border border-primary/20 text-primary text-xs font-bold uppercase tracking-widest"${_scopeId}>`);
						_push(ssrRenderComponent(unref(Sparkles), { class: "w-3 h-3" }, null, _parent, _scopeId));
						_push(` Visi Kami </div><h2 class="text-3xl font-bold text-white tracking-tight"${_scopeId}>Menghapus Batas Antara Ide dan Eksekusi.</h2><p class="text-zinc-400 leading-relaxed"${_scopeId}> Kami percaya bahwa setiap pemilik bisnis layak mendapatkan alat yang sama kuatnya dengan perusahaan raksasa. Misi kami adalah mendemokratisasi teknologi enterprise dan mengemasnya dalam antarmuka yang sangat mudah digunakan oleh siapa saja. </p><div class="flex gap-4"${_scopeId}><div class="text-center p-4 rounded-2xl bg-zinc-900/50 border border-white/5 flex-1"${_scopeId}><div class="text-2xl font-bold text-white"${_scopeId}>500+</div><div class="text-[10px] text-zinc-500 uppercase font-bold tracking-widest mt-1"${_scopeId}>Mitra Bisnis</div></div><div class="text-center p-4 rounded-2xl bg-zinc-900/50 border border-white/5 flex-1"${_scopeId}><div class="text-2xl font-bold text-white"${_scopeId}>99.9%</div><div class="text-[10px] text-zinc-500 uppercase font-bold tracking-widest mt-1"${_scopeId}>Uptime</div></div></div></div><div class="relative group"${_scopeId}><div class="absolute -inset-4 bg-primary/20 blur-3xl rounded-full opacity-0 group-hover:opacity-100 transition-opacity duration-700"${_scopeId}></div><div class="relative aspect-square rounded-3xl bg-zinc-900 border border-white/10 overflow-hidden flex items-center justify-center shadow-2xl"${_scopeId}>`);
						_push(ssrRenderComponent(unref(Zap), { class: "w-32 h-32 text-primary opacity-20 animate-pulse" }, null, _parent, _scopeId));
						_push(`<div class="absolute inset-0 flex items-center justify-center p-8"${_scopeId}><div class="text-center space-y-4"${_scopeId}>`);
						_push(ssrRenderComponent(unref(Users), { class: "w-12 h-12 text-white mx-auto mb-4" }, null, _parent, _scopeId));
						_push(`<p class="text-sm font-medium text-zinc-300 italic"${_scopeId}>&quot;Teknologi yang hebat adalah teknologi yang tidak terasa seperti teknologi—ia hanya bekerja.&quot;</p></div></div></div></div></div><div class="space-y-12"${_scopeId}><div class="text-center space-y-4"${_scopeId}><h2 class="text-3xl font-bold text-white"${_scopeId}>Nilai Inti Kami</h2><p class="text-zinc-400 max-w-xl mx-auto"${_scopeId}>Prinsip yang membimbing kami dalam membangun setiap baris kode di Gaptek.</p></div><div class="grid grid-cols-1 md:grid-cols-3 gap-6"${_scopeId}><div class="p-8 rounded-3xl bg-zinc-900 border border-white/5 hover:border-primary/30 transition-all group"${_scopeId}><div class="w-12 h-12 rounded-2xl bg-primary/10 flex items-center justify-center mb-6 group-hover:scale-110 transition-transform"${_scopeId}>`);
						_push(ssrRenderComponent(unref(Rocket), { class: "w-6 h-6 text-primary" }, null, _parent, _scopeId));
						_push(`</div><h3 class="text-lg font-bold text-white mb-2"${_scopeId}>Inovasi Tanpa Henti</h3><p class="text-sm text-zinc-500 leading-relaxed"${_scopeId}>Kami tidak pernah puas dengan status quo. Setiap minggu ada fitur baru yang lahir dari masukan Anda.</p></div><div class="p-8 rounded-3xl bg-zinc-900 border border-white/5 hover:border-rose-500/30 transition-all group"${_scopeId}><div class="w-12 h-12 rounded-2xl bg-rose-500/10 flex items-center justify-center mb-6 group-hover:scale-110 transition-transform"${_scopeId}>`);
						_push(ssrRenderComponent(unref(Heart), { class: "w-6 h-6 text-rose-500" }, null, _parent, _scopeId));
						_push(`</div><h3 class="text-lg font-bold text-white mb-2"${_scopeId}>Empati Pengguna</h3><p class="text-sm text-zinc-500 leading-relaxed"${_scopeId}>Kami membangun solusi berdasarkan masalah nyata yang dihadapi oleh pemilik toko dan manajer gudang.</p></div><div class="p-8 rounded-3xl bg-zinc-900 border border-white/5 hover:border-blue-500/30 transition-all group"${_scopeId}><div class="w-12 h-12 rounded-2xl bg-blue-500/10 flex items-center justify-center mb-6 group-hover:scale-110 transition-transform"${_scopeId}>`);
						_push(ssrRenderComponent(unref(Shield), { class: "w-6 h-6 text-blue-500" }, null, _parent, _scopeId));
						_push(`</div><h3 class="text-lg font-bold text-white mb-2"${_scopeId}>Kepercayaan &amp; Keamanan</h3><p class="text-sm text-zinc-500 leading-relaxed"${_scopeId}>Data Anda adalah aset paling berharga. Kami melindunginya dengan standar keamanan militer.</p></div></div></div><div class="relative p-12 rounded-[3rem] bg-zinc-900 border border-white/10 overflow-hidden text-center space-y-8"${_scopeId}><div class="absolute inset-0 bg-gradient-to-b from-primary/5 to-transparent"${_scopeId}></div>`);
						_push(ssrRenderComponent(unref(Trophy), { class: "w-16 h-16 text-primary mx-auto relative z-10" }, null, _parent, _scopeId));
						_push(`<h2 class="text-4xl font-bold text-white relative z-10"${_scopeId}>Siap Bergabung dengan Revolusi?</h2><p class="text-zinc-400 max-w-lg mx-auto relative z-10"${_scopeId}>Ribuan pebisnis telah beralih ke Gaptek untuk menyederhanakan hidup mereka. Sekarang giliran Anda.</p><div class="relative z-10"${_scopeId}>`);
						_push(ssrRenderComponent(_sfc_main$1, {
							class: "h-14 px-10 rounded-full bg-white text-zinc-900 hover:scale-105 transition-transform duration-300 font-bold text-lg",
							onClick: ($event) => unref(router).push("/login")
						}, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) _push(` Mulai Perjalanan Anda `);
								else return [createTextVNode(" Mulai Perjalanan Anda ")];
							}),
							_: 1
						}, _parent, _scopeId));
						_push(`</div></div></div></main><footer class="py-12 border-t border-white/5 text-center"${_scopeId}><div class="flex items-center justify-center gap-2 mb-4"${_scopeId}>`);
						_push(ssrRenderComponent(unref(Zap), { class: "w-5 h-5 text-primary" }, null, _parent, _scopeId));
						_push(`<span class="text-lg font-bold text-white tracking-tighter uppercase"${_scopeId}>Gaptek</span></div><p class="text-xs text-zinc-600"${_scopeId}>© 2026 Gaptek Technology Inc. Semua hak dilindungi undang-undang.</p></footer>`);
					} else return [
						createVNode(Navbar_default, {
							"brand-name": "gaptek",
							"nav-items": navigationItems,
							onNavigate: handleNavigation
						}, {
							logo: withCtx(() => [createVNode("div", { class: "h-full w-full bg-zinc-100 flex items-center justify-center" }, [createVNode(unref(Zap), { class: "h-4 w-4 text-zinc-900" })])]),
							actions: withCtx(() => [createVNode(_sfc_main$1, {
								variant: "ghost",
								class: "hidden sm:inline-flex font-bold text-zinc-300 hover:text-white transition-colors duration-300",
								onClick: ($event) => unref(router).push("/login")
							}, {
								default: withCtx(() => [createTextVNode(" Masuk ")]),
								_: 1
							}, 8, ["onClick"]), createVNode(_sfc_main$1, {
								class: "font-bold px-6 py-2 rounded-xl bg-zinc-100 text-zinc-900 hover:bg-white transition-all active:scale-95",
								onClick: ($event) => unref(router).push("/login")
							}, {
								default: withCtx(() => [createTextVNode(" Mulai Gratis ")]),
								_: 1
							}, 8, ["onClick"])]),
							_: 1
						}),
						createVNode("main", { class: "relative flex-grow flex flex-col items-center pt-32 pb-24 px-6 overflow-hidden" }, [createVNode("div", { class: "absolute inset-0 z-0 opacity-40 pointer-events-none" }, [createVNode(GradientBlinds_default, {
							"gradient-colors": ["#1EA03F", "#182FFF"],
							angle: 0,
							noise: .2,
							"blind-count": 10,
							"mix-blend-mode": "screen"
						})]), createVNode("div", { class: "relative z-10 max-w-4xl mx-auto space-y-24" }, [
							createVNode("div", { class: "text-center space-y-8" }, [
								createVNode("button", {
									onClick: navigateBack,
									class: "inline-flex items-center gap-2 text-sm font-medium text-zinc-500 hover:text-white transition-colors mb-4 group"
								}, [createVNode(unref(ArrowLeft), { class: "w-4 h-4 group-hover:-translate-x-1 transition-transform" }), createTextVNode(" Kembali ")]),
								createVNode("h1", { class: "text-5xl md:text-7xl font-serif italic tracking-tight text-white leading-tight" }, [
									createTextVNode(" Kami Membangun "),
									createVNode("span", { class: "text-primary not-italic" }, "Masa Depan"),
									createTextVNode(" Retail. ")
								]),
								createVNode("p", { class: "text-xl text-zinc-400 font-medium max-w-2xl mx-auto leading-relaxed" }, " Gaptek hadir sebagai solusi revolusioner untuk membantu pengusaha retail dan F&B naik kelas melalui teknologi otomasi dan kecerdasan data yang elegan. ")
							]),
							createVNode("div", { class: "grid grid-cols-1 md:grid-cols-2 gap-12 items-center" }, [createVNode("div", { class: "space-y-6" }, [
								createVNode("div", { class: "inline-flex items-center gap-2 px-3 py-1 rounded-full bg-primary/10 border border-primary/20 text-primary text-xs font-bold uppercase tracking-widest" }, [createVNode(unref(Sparkles), { class: "w-3 h-3" }), createTextVNode(" Visi Kami ")]),
								createVNode("h2", { class: "text-3xl font-bold text-white tracking-tight" }, "Menghapus Batas Antara Ide dan Eksekusi."),
								createVNode("p", { class: "text-zinc-400 leading-relaxed" }, " Kami percaya bahwa setiap pemilik bisnis layak mendapatkan alat yang sama kuatnya dengan perusahaan raksasa. Misi kami adalah mendemokratisasi teknologi enterprise dan mengemasnya dalam antarmuka yang sangat mudah digunakan oleh siapa saja. "),
								createVNode("div", { class: "flex gap-4" }, [createVNode("div", { class: "text-center p-4 rounded-2xl bg-zinc-900/50 border border-white/5 flex-1" }, [createVNode("div", { class: "text-2xl font-bold text-white" }, "500+"), createVNode("div", { class: "text-[10px] text-zinc-500 uppercase font-bold tracking-widest mt-1" }, "Mitra Bisnis")]), createVNode("div", { class: "text-center p-4 rounded-2xl bg-zinc-900/50 border border-white/5 flex-1" }, [createVNode("div", { class: "text-2xl font-bold text-white" }, "99.9%"), createVNode("div", { class: "text-[10px] text-zinc-500 uppercase font-bold tracking-widest mt-1" }, "Uptime")])])
							]), createVNode("div", { class: "relative group" }, [createVNode("div", { class: "absolute -inset-4 bg-primary/20 blur-3xl rounded-full opacity-0 group-hover:opacity-100 transition-opacity duration-700" }), createVNode("div", { class: "relative aspect-square rounded-3xl bg-zinc-900 border border-white/10 overflow-hidden flex items-center justify-center shadow-2xl" }, [createVNode(unref(Zap), { class: "w-32 h-32 text-primary opacity-20 animate-pulse" }), createVNode("div", { class: "absolute inset-0 flex items-center justify-center p-8" }, [createVNode("div", { class: "text-center space-y-4" }, [createVNode(unref(Users), { class: "w-12 h-12 text-white mx-auto mb-4" }), createVNode("p", { class: "text-sm font-medium text-zinc-300 italic" }, "\"Teknologi yang hebat adalah teknologi yang tidak terasa seperti teknologi—ia hanya bekerja.\"")])])])])]),
							createVNode("div", { class: "space-y-12" }, [createVNode("div", { class: "text-center space-y-4" }, [createVNode("h2", { class: "text-3xl font-bold text-white" }, "Nilai Inti Kami"), createVNode("p", { class: "text-zinc-400 max-w-xl mx-auto" }, "Prinsip yang membimbing kami dalam membangun setiap baris kode di Gaptek.")]), createVNode("div", { class: "grid grid-cols-1 md:grid-cols-3 gap-6" }, [
								createVNode("div", { class: "p-8 rounded-3xl bg-zinc-900 border border-white/5 hover:border-primary/30 transition-all group" }, [
									createVNode("div", { class: "w-12 h-12 rounded-2xl bg-primary/10 flex items-center justify-center mb-6 group-hover:scale-110 transition-transform" }, [createVNode(unref(Rocket), { class: "w-6 h-6 text-primary" })]),
									createVNode("h3", { class: "text-lg font-bold text-white mb-2" }, "Inovasi Tanpa Henti"),
									createVNode("p", { class: "text-sm text-zinc-500 leading-relaxed" }, "Kami tidak pernah puas dengan status quo. Setiap minggu ada fitur baru yang lahir dari masukan Anda.")
								]),
								createVNode("div", { class: "p-8 rounded-3xl bg-zinc-900 border border-white/5 hover:border-rose-500/30 transition-all group" }, [
									createVNode("div", { class: "w-12 h-12 rounded-2xl bg-rose-500/10 flex items-center justify-center mb-6 group-hover:scale-110 transition-transform" }, [createVNode(unref(Heart), { class: "w-6 h-6 text-rose-500" })]),
									createVNode("h3", { class: "text-lg font-bold text-white mb-2" }, "Empati Pengguna"),
									createVNode("p", { class: "text-sm text-zinc-500 leading-relaxed" }, "Kami membangun solusi berdasarkan masalah nyata yang dihadapi oleh pemilik toko dan manajer gudang.")
								]),
								createVNode("div", { class: "p-8 rounded-3xl bg-zinc-900 border border-white/5 hover:border-blue-500/30 transition-all group" }, [
									createVNode("div", { class: "w-12 h-12 rounded-2xl bg-blue-500/10 flex items-center justify-center mb-6 group-hover:scale-110 transition-transform" }, [createVNode(unref(Shield), { class: "w-6 h-6 text-blue-500" })]),
									createVNode("h3", { class: "text-lg font-bold text-white mb-2" }, "Kepercayaan & Keamanan"),
									createVNode("p", { class: "text-sm text-zinc-500 leading-relaxed" }, "Data Anda adalah aset paling berharga. Kami melindunginya dengan standar keamanan militer.")
								])
							])]),
							createVNode("div", { class: "relative p-12 rounded-[3rem] bg-zinc-900 border border-white/10 overflow-hidden text-center space-y-8" }, [
								createVNode("div", { class: "absolute inset-0 bg-gradient-to-b from-primary/5 to-transparent" }),
								createVNode(unref(Trophy), { class: "w-16 h-16 text-primary mx-auto relative z-10" }),
								createVNode("h2", { class: "text-4xl font-bold text-white relative z-10" }, "Siap Bergabung dengan Revolusi?"),
								createVNode("p", { class: "text-zinc-400 max-w-lg mx-auto relative z-10" }, "Ribuan pebisnis telah beralih ke Gaptek untuk menyederhanakan hidup mereka. Sekarang giliran Anda."),
								createVNode("div", { class: "relative z-10" }, [createVNode(_sfc_main$1, {
									class: "h-14 px-10 rounded-full bg-white text-zinc-900 hover:scale-105 transition-transform duration-300 font-bold text-lg",
									onClick: ($event) => unref(router).push("/login")
								}, {
									default: withCtx(() => [createTextVNode(" Mulai Perjalanan Anda ")]),
									_: 1
								}, 8, ["onClick"])])
							])
						])]),
						createVNode("footer", { class: "py-12 border-t border-white/5 text-center" }, [createVNode("div", { class: "flex items-center justify-center gap-2 mb-4" }, [createVNode(unref(Zap), { class: "w-5 h-5 text-primary" }), createVNode("span", { class: "text-lg font-bold text-white tracking-tighter uppercase" }, "Gaptek")]), createVNode("p", { class: "text-xs text-zinc-600" }, "© 2026 Gaptek Technology Inc. Semua hak dilindungi undang-undang.")])
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
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/pages/AboutPage.vue");
	return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
//#endregion
export { _sfc_main as default };
