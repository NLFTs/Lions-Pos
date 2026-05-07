import { t as _plugin_vue_export_helper_default } from "./_plugin-vue_export-helper-DMwexRDj.js";
import { s as _sfc_main$2, t as _sfc_main$1 } from "./AppLayout-D1IhsFmL.js";
import { t as useToast } from "./useToast-BeMK7Zjj.js";
import { t as _sfc_main$3 } from "./Input-yu8tAo3O.js";
import { Fragment, TransitionGroup, computed, createBlock, createCommentVNode, createTextVNode, createVNode, openBlock, ref, renderList, toDisplayString, unref, useSSRContext, withCtx } from "vue";
import { ssrInterpolate, ssrRenderAttr, ssrRenderClass, ssrRenderComponent, ssrRenderList, ssrRenderStyle } from "vue/server-renderer";
import { ArrowLeft, CreditCard, Minus, Plus, Search, ShoppingBag, ShoppingCart, Trash2 } from "lucide-vue-next";
//#region src/pages/KasirPage.vue
var _sfc_main = {
	__name: "KasirPage",
	__ssrInlineRender: true,
	setup(__props) {
		const { toast } = useToast();
		const MOCK_PRODUCTS = [
			{
				id: "1",
				name: "Kaos Polos Putih",
				sku: "KPP-001",
				price: 85e3,
				categoryName: "Pakaian",
				isActive: true,
				imageUrl: "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=300&q=80"
			},
			{
				id: "2",
				name: "Celana Chino Beige",
				sku: "CCB-002",
				price: 195e3,
				categoryName: "Pakaian",
				isActive: true,
				imageUrl: "https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=300&q=80"
			},
			{
				id: "3",
				name: "Sepatu Sneakers Hitam",
				sku: "SSH-003",
				price: 45e4,
				categoryName: "Alas Kaki",
				isActive: true,
				imageUrl: null
			},
			{
				id: "5",
				name: "Jaket Bomber Olive",
				sku: "JBO-005",
				price: 32e4,
				categoryName: "Pakaian",
				isActive: true,
				imageUrl: null
			},
			{
				id: "6",
				name: "Tas Selempang Canvas",
				sku: "TSC-006",
				price: 135e3,
				categoryName: "Tas",
				isActive: true,
				imageUrl: null
			},
			{
				id: "7",
				name: "Kemeja Flannel Kotak",
				sku: "KFK-007",
				price: 21e4,
				categoryName: "Pakaian",
				isActive: true,
				imageUrl: null
			},
			{
				id: "8",
				name: "Kaos Kaki Sport (3 pcs)",
				sku: "KKS-008",
				price: 45e3,
				categoryName: "Aksesoris",
				isActive: true,
				imageUrl: null
			},
			{
				id: "9",
				name: "Sabuk Kulit Coklat",
				sku: "SKC-009",
				price: 98e3,
				categoryName: "Aksesoris",
				isActive: true,
				imageUrl: null
			}
		];
		const searchQuery = ref("");
		const showCartMobile = ref(false);
		const groupedProducts = computed(() => {
			let result = MOCK_PRODUCTS.filter((p) => p.isActive);
			if (searchQuery.value) {
				const q = searchQuery.value.toLowerCase();
				result = result.filter((p) => p.name.toLowerCase().includes(q) || p.sku && p.sku.toLowerCase().includes(q));
			}
			const groups = {};
			result.forEach((p) => {
				const cat = p.categoryName || "Lainnya";
				if (!groups[cat]) groups[cat] = [];
				groups[cat].push(p);
			});
			return Object.keys(groups).map((key) => ({
				category: key,
				products: groups[key]
			})).sort((a, b) => a.category.localeCompare(b.category));
		});
		const cart = ref([]);
		function addToCart(product) {
			const existing = cart.value.find((item) => item.id === product.id);
			if (existing) existing.qty++;
			else cart.value.push({
				...product,
				qty: 1
			});
		}
		function getCartQty(id) {
			const item = cart.value.find((i) => i.id === id);
			return item ? item.qty : 0;
		}
		function increaseQty(item) {
			item.qty++;
		}
		function decreaseQty(item) {
			if (item.qty > 1) item.qty--;
			else removeFromCart(item);
		}
		function removeFromCart(item) {
			const index = cart.value.findIndex((i) => i.id === item.id);
			if (index !== -1) cart.value.splice(index, 1);
		}
		function formatCurrency(value) {
			return new Intl.NumberFormat("id-ID", {
				style: "currency",
				currency: "IDR",
				minimumFractionDigits: 0
			}).format(value);
		}
		const subtotal = computed(() => cart.value.reduce((sum, item) => sum + item.price * item.qty, 0));
		const tax = computed(() => subtotal.value * .11);
		const total = computed(() => subtotal.value + tax.value);
		function checkout() {
			if (cart.value.length === 0) return;
			toast.success(`Pembayaran berhasil! Total: ${formatCurrency(total.value)}`);
			cart.value = [];
			showCartMobile.value = false;
		}
		const AVATAR_COLORS = [
			{
				bg: "#ede9fe",
				color: "#6d28d9"
			},
			{
				bg: "#dbeafe",
				color: "#1d4ed8"
			},
			{
				bg: "#d1fae5",
				color: "#065f46"
			},
			{
				bg: "#fef3c7",
				color: "#92400e"
			},
			{
				bg: "#fee2e2",
				color: "#991b1b"
			},
			{
				bg: "#fce7f3",
				color: "#9d174d"
			},
			{
				bg: "#e0f2fe",
				color: "#0369a1"
			},
			{
				bg: "#f3f4f6",
				color: "#374151"
			}
		];
		function productAvatarStyle(name = "") {
			const c = AVATAR_COLORS[name.charCodeAt(0) % AVATAR_COLORS.length];
			return {
				backgroundColor: c.bg,
				color: c.color
			};
		}
		return (_ctx, _push, _parent, _attrs) => {
			_push(ssrRenderComponent(_sfc_main$1, _attrs, {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) {
						_push(`<div class="h-[calc(100dvh-3rem)] p-2 sm:p-4 pb-2 lg:pb-4 flex flex-col lg:flex-row gap-2 sm:gap-4 bg-zinc-50/50 dark:bg-zinc-950 overflow-hidden" data-v-cb5cef7c${_scopeId}><div class="${ssrRenderClass([showCartMobile.value ? "hidden" : "flex", "flex-1 flex-col min-w-0 min-h-0 bg-white dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 rounded-xl shadow-sm overflow-hidden lg:flex"])}" data-v-cb5cef7c${_scopeId}><div class="p-4 border-b border-zinc-200 dark:border-zinc-800 shrink-0" data-v-cb5cef7c${_scopeId}><div class="flex flex-col sm:flex-row gap-3 justify-between items-start sm:items-center" data-v-cb5cef7c${_scopeId}><div data-v-cb5cef7c${_scopeId}><h1 class="text-xl font-bold tracking-tight text-zinc-900 dark:text-zinc-100" data-v-cb5cef7c${_scopeId}>Kasir</h1><p class="text-xs text-muted-foreground mt-0.5" data-v-cb5cef7c${_scopeId}>Sistem Point of Sale</p></div><div class="relative w-full sm:w-64" data-v-cb5cef7c${_scopeId}>`);
						_push(ssrRenderComponent(unref(Search), { class: "absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-zinc-400" }, null, _parent, _scopeId));
						_push(ssrRenderComponent(_sfc_main$3, {
							modelValue: searchQuery.value,
							"onUpdate:modelValue": ($event) => searchQuery.value = $event,
							placeholder: "Cari produk...",
							class: "pl-9 bg-zinc-100 dark:bg-zinc-900 border-transparent focus:bg-white dark:focus:bg-zinc-950"
						}, null, _parent, _scopeId));
						_push(`</div></div></div><div class="flex-1 overflow-y-auto p-4 custom-scrollbar bg-zinc-50/30 dark:bg-zinc-950/50" data-v-cb5cef7c${_scopeId}>`);
						if (groupedProducts.value.length === 0) {
							_push(`<div class="h-full flex flex-col items-center justify-center text-muted-foreground" data-v-cb5cef7c${_scopeId}>`);
							_push(ssrRenderComponent(unref(ShoppingBag), { class: "h-12 w-12 opacity-20 mb-3" }, null, _parent, _scopeId));
							_push(`<p class="text-sm" data-v-cb5cef7c${_scopeId}>Produk tidak ditemukan.</p></div>`);
						} else {
							_push(`<div class="space-y-6" data-v-cb5cef7c${_scopeId}><!--[-->`);
							ssrRenderList(groupedProducts.value, (group) => {
								_push(`<div data-v-cb5cef7c${_scopeId}><h2 class="text-[13px] font-bold text-zinc-800 dark:text-zinc-200 mb-3 uppercase tracking-wider flex items-center gap-3" data-v-cb5cef7c${_scopeId}><span data-v-cb5cef7c${_scopeId}>${ssrInterpolate(group.category)}</span><div class="h-px bg-zinc-200 dark:bg-zinc-800 flex-1 mt-0.5" data-v-cb5cef7c${_scopeId}></div></h2><div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 xl:grid-cols-5 gap-3" data-v-cb5cef7c${_scopeId}><!--[-->`);
								ssrRenderList(group.products, (product) => {
									_push(`<div class="${ssrRenderClass([getCartQty(product.id) > 0 ? "border-primary ring-1 ring-primary/20 shadow-sm" : "border-zinc-200 dark:border-zinc-800 hover:border-primary/50 hover:bg-primary/5", "group relative flex flex-col bg-white dark:bg-zinc-900 border rounded-lg overflow-hidden cursor-pointer transition-all duration-200"])}" data-v-cb5cef7c${_scopeId}>`);
									if (getCartQty(product.id) > 0) _push(`<div class="absolute top-1.5 right-1.5 z-10 bg-primary text-primary-foreground text-[10px] font-bold min-w-[20px] h-[20px] flex items-center justify-center rounded-full shadow-md border border-primary-foreground/20 px-1" data-v-cb5cef7c${_scopeId}>${ssrInterpolate(getCartQty(product.id))}</div>`);
									else _push(`<!---->`);
									_push(`<div class="${ssrRenderClass([getCartQty(product.id) > 0 ? "opacity-90" : "", "h-24 bg-zinc-100 dark:bg-zinc-800 relative overflow-hidden flex items-center justify-center shrink-0 border-b border-zinc-100 dark:border-zinc-800"])}" data-v-cb5cef7c${_scopeId}>`);
									if (product.imageUrl) _push(`<img${ssrRenderAttr("src", product.imageUrl)} class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300" data-v-cb5cef7c${_scopeId}>`);
									else _push(`<div class="w-full h-full flex items-center justify-center text-2xl font-bold select-none opacity-80" style="${ssrRenderStyle(productAvatarStyle(product.name))}" data-v-cb5cef7c${_scopeId}>${ssrInterpolate(product.name.charAt(0).toUpperCase())}</div>`);
									_push(`</div><div class="p-2.5 flex flex-col flex-1 justify-between gap-1.5" data-v-cb5cef7c${_scopeId}><h3 class="font-medium text-xs text-zinc-900 dark:text-zinc-100 line-clamp-2 leading-snug group-hover:text-primary transition-colors" data-v-cb5cef7c${_scopeId}>${ssrInterpolate(product.name)}</h3><div class="flex items-end justify-between mt-auto pt-1" data-v-cb5cef7c${_scopeId}><span class="text-[9px] text-zinc-400 font-mono" data-v-cb5cef7c${_scopeId}>${ssrInterpolate(product.sku || "")}</span><span class="text-[11px] font-bold text-zinc-800 dark:text-zinc-200" data-v-cb5cef7c${_scopeId}>${ssrInterpolate(formatCurrency(product.price))}</span></div></div></div>`);
								});
								_push(`<!--]--></div></div>`);
							});
							_push(`<!--]--></div>`);
						}
						_push(`</div>`);
						if (cart.value.length > 0) {
							_push(`<div class="lg:hidden p-3 border-t border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-950 shrink-0 shadow-[0_-4px_10px_rgba(0,0,0,0.05)]" data-v-cb5cef7c${_scopeId}>`);
							_push(ssrRenderComponent(_sfc_main$2, {
								onClick: ($event) => showCartMobile.value = true,
								class: "w-full h-12 flex justify-between items-center text-[13px] font-bold shadow-md"
							}, {
								default: withCtx((_, _push, _parent, _scopeId) => {
									if (_push) {
										_push(`<div class="flex items-center gap-2" data-v-cb5cef7c${_scopeId}>`);
										_push(ssrRenderComponent(unref(ShoppingCart), { class: "h-4 w-4" }, null, _parent, _scopeId));
										_push(`<span data-v-cb5cef7c${_scopeId}>${ssrInterpolate(cart.value.reduce((sum, item) => sum + item.qty, 0))} Item</span></div><span data-v-cb5cef7c${_scopeId}>${ssrInterpolate(formatCurrency(total.value))}</span>`);
									} else return [createVNode("div", { class: "flex items-center gap-2" }, [createVNode(unref(ShoppingCart), { class: "h-4 w-4" }), createVNode("span", null, toDisplayString(cart.value.reduce((sum, item) => sum + item.qty, 0)) + " Item", 1)]), createVNode("span", null, toDisplayString(formatCurrency(total.value)), 1)];
								}),
								_: 1
							}, _parent, _scopeId));
							_push(`</div>`);
						} else _push(`<!---->`);
						_push(`</div><div class="${ssrRenderClass([showCartMobile.value ? "flex flex-1" : "hidden", "w-full lg:w-[300px] xl:w-[340px] flex-col shrink-0 bg-white dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 rounded-xl shadow-sm lg:h-auto overflow-hidden lg:flex"])}" data-v-cb5cef7c${_scopeId}><div class="p-3 border-b border-zinc-200 dark:border-zinc-800 shrink-0 flex items-center justify-between" data-v-cb5cef7c${_scopeId}><div class="flex items-center gap-2" data-v-cb5cef7c${_scopeId}><button class="lg:hidden p-1 -ml-1 mr-1 text-zinc-500 hover:bg-zinc-100 dark:hover:bg-zinc-800 rounded-md transition-colors" data-v-cb5cef7c${_scopeId}>`);
						_push(ssrRenderComponent(unref(ArrowLeft), { class: "h-5 w-5" }, null, _parent, _scopeId));
						_push(`</button>`);
						_push(ssrRenderComponent(unref(ShoppingCart), { class: "h-5 w-5 text-primary hidden lg:block" }, null, _parent, _scopeId));
						_push(`<h2 class="font-bold text-lg" data-v-cb5cef7c${_scopeId}>Pesanan</h2><span class="bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-400 text-[10px] font-bold px-2 py-0.5 rounded-full ml-1" data-v-cb5cef7c${_scopeId}>${ssrInterpolate(cart.value.reduce((sum, item) => sum + item.qty, 0))} Item </span></div>`);
						if (cart.value.length > 0) _push(`<button class="text-[11px] font-medium text-red-500 hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-500/10 px-2 py-1 rounded transition-colors" data-v-cb5cef7c${_scopeId}> Kosongkan </button>`);
						else _push(`<!---->`);
						_push(`</div><div class="flex-1 overflow-y-auto p-2 sm:p-3 custom-scrollbar bg-zinc-50/50 dark:bg-zinc-950/50" data-v-cb5cef7c${_scopeId}>`);
						if (cart.value.length === 0) {
							_push(`<div class="h-full flex flex-col items-center justify-center text-muted-foreground space-y-3" data-v-cb5cef7c${_scopeId}><div class="w-16 h-16 rounded-full bg-zinc-100 dark:bg-zinc-900 flex items-center justify-center" data-v-cb5cef7c${_scopeId}>`);
							_push(ssrRenderComponent(unref(ShoppingCart), { class: "h-8 w-8 opacity-20" }, null, _parent, _scopeId));
							_push(`</div><p class="text-sm" data-v-cb5cef7c${_scopeId}>Keranjang masih kosong</p></div>`);
						} else {
							_push(`<div class="space-y-3" data-v-cb5cef7c${_scopeId}><!--[-->`);
							ssrRenderList(cart.value, (item) => {
								_push(`<div class="flex flex-col bg-white dark:bg-zinc-900 border border-zinc-100 dark:border-zinc-800/80 p-2 rounded-lg shadow-sm" data-v-cb5cef7c${_scopeId}><div class="flex items-start justify-between gap-1.5" data-v-cb5cef7c${_scopeId}><div class="flex-1 min-w-0" data-v-cb5cef7c${_scopeId}><h4 class="font-semibold text-[11px] text-zinc-900 dark:text-zinc-100 line-clamp-2 leading-tight" data-v-cb5cef7c${_scopeId}>${ssrInterpolate(item.name)}</h4><p class="text-[9px] text-primary font-semibold mt-0.5" data-v-cb5cef7c${_scopeId}>${ssrInterpolate(formatCurrency(item.price))}</p></div><button class="p-1 text-zinc-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-500/10 rounded-md transition-colors shrink-0" data-v-cb5cef7c${_scopeId}>`);
								_push(ssrRenderComponent(unref(Trash2), { class: "h-3 w-3" }, null, _parent, _scopeId));
								_push(`</button></div><div class="flex items-center justify-between mt-1.5" data-v-cb5cef7c${_scopeId}><div class="flex items-center bg-zinc-100 dark:bg-zinc-800/80 rounded p-0.5 border border-zinc-200 dark:border-zinc-700/50" data-v-cb5cef7c${_scopeId}><button class="w-5 h-5 flex items-center justify-center rounded-[3px] hover:bg-white dark:hover:bg-zinc-700 text-zinc-600 dark:text-zinc-300 shadow-sm transition-colors" data-v-cb5cef7c${_scopeId}>`);
								_push(ssrRenderComponent(unref(Minus), { class: "h-[10px] w-[10px]" }, null, _parent, _scopeId));
								_push(`</button><span class="w-5 text-center text-[10px] font-bold text-zinc-900 dark:text-zinc-100" data-v-cb5cef7c${_scopeId}>${ssrInterpolate(item.qty)}</span><button class="w-5 h-5 flex items-center justify-center rounded-[3px] hover:bg-white dark:hover:bg-zinc-700 text-zinc-600 dark:text-zinc-300 shadow-sm transition-colors" data-v-cb5cef7c${_scopeId}>`);
								_push(ssrRenderComponent(unref(Plus), { class: "h-[10px] w-[10px]" }, null, _parent, _scopeId));
								_push(`</button></div><span class="font-bold text-[11px] text-zinc-900 dark:text-zinc-100" data-v-cb5cef7c${_scopeId}>${ssrInterpolate(formatCurrency(item.price * item.qty))}</span></div></div>`);
							});
							_push(`<!--]--></div>`);
						}
						_push(`</div><div class="p-2 sm:p-3 border-t border-zinc-200 dark:border-zinc-800 shrink-0 bg-zinc-50/50 dark:bg-zinc-900/30" data-v-cb5cef7c${_scopeId}><div class="space-y-1 mb-2" data-v-cb5cef7c${_scopeId}><div class="flex justify-between text-xs" data-v-cb5cef7c${_scopeId}><span class="text-zinc-500 font-medium" data-v-cb5cef7c${_scopeId}>Subtotal</span><span class="font-semibold text-zinc-800 dark:text-zinc-200" data-v-cb5cef7c${_scopeId}>${ssrInterpolate(formatCurrency(subtotal.value))}</span></div><div class="flex justify-between text-xs" data-v-cb5cef7c${_scopeId}><span class="text-zinc-500 font-medium" data-v-cb5cef7c${_scopeId}>Pajak (11%)</span><span class="font-semibold text-zinc-800 dark:text-zinc-200" data-v-cb5cef7c${_scopeId}>${ssrInterpolate(formatCurrency(tax.value))}</span></div><div class="h-px w-full bg-zinc-200 dark:bg-zinc-800 my-1.5" data-v-cb5cef7c${_scopeId}></div><div class="flex justify-between items-center" data-v-cb5cef7c${_scopeId}><span class="text-[13px] font-bold text-zinc-900 dark:text-zinc-100" data-v-cb5cef7c${_scopeId}>Total</span><span class="text-lg font-black text-primary" data-v-cb5cef7c${_scopeId}>${ssrInterpolate(formatCurrency(total.value))}</span></div></div>`);
						_push(ssrRenderComponent(_sfc_main$2, {
							onClick: checkout,
							class: "w-full h-9 text-[13px] font-bold shadow-md hover:shadow-lg transition-all",
							disabled: cart.value.length === 0
						}, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) {
									_push(ssrRenderComponent(unref(CreditCard), { class: "h-4 w-4 mr-1.5" }, null, _parent, _scopeId));
									_push(` Bayar Pesanan `);
								} else return [createVNode(unref(CreditCard), { class: "h-4 w-4 mr-1.5" }), createTextVNode(" Bayar Pesanan ")];
							}),
							_: 1
						}, _parent, _scopeId));
						_push(`</div></div></div>`);
					} else return [createVNode("div", { class: "h-[calc(100dvh-3rem)] p-2 sm:p-4 pb-2 lg:pb-4 flex flex-col lg:flex-row gap-2 sm:gap-4 bg-zinc-50/50 dark:bg-zinc-950 overflow-hidden" }, [createVNode("div", { class: ["flex-1 flex-col min-w-0 min-h-0 bg-white dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 rounded-xl shadow-sm overflow-hidden lg:flex", showCartMobile.value ? "hidden" : "flex"] }, [
						createVNode("div", { class: "p-4 border-b border-zinc-200 dark:border-zinc-800 shrink-0" }, [createVNode("div", { class: "flex flex-col sm:flex-row gap-3 justify-between items-start sm:items-center" }, [createVNode("div", null, [createVNode("h1", { class: "text-xl font-bold tracking-tight text-zinc-900 dark:text-zinc-100" }, "Kasir"), createVNode("p", { class: "text-xs text-muted-foreground mt-0.5" }, "Sistem Point of Sale")]), createVNode("div", { class: "relative w-full sm:w-64" }, [createVNode(unref(Search), { class: "absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-zinc-400" }), createVNode(_sfc_main$3, {
							modelValue: searchQuery.value,
							"onUpdate:modelValue": ($event) => searchQuery.value = $event,
							placeholder: "Cari produk...",
							class: "pl-9 bg-zinc-100 dark:bg-zinc-900 border-transparent focus:bg-white dark:focus:bg-zinc-950"
						}, null, 8, ["modelValue", "onUpdate:modelValue"])])])]),
						createVNode("div", { class: "flex-1 overflow-y-auto p-4 custom-scrollbar bg-zinc-50/30 dark:bg-zinc-950/50" }, [groupedProducts.value.length === 0 ? (openBlock(), createBlock("div", {
							key: 0,
							class: "h-full flex flex-col items-center justify-center text-muted-foreground"
						}, [createVNode(unref(ShoppingBag), { class: "h-12 w-12 opacity-20 mb-3" }), createVNode("p", { class: "text-sm" }, "Produk tidak ditemukan.")])) : (openBlock(), createBlock("div", {
							key: 1,
							class: "space-y-6"
						}, [(openBlock(true), createBlock(Fragment, null, renderList(groupedProducts.value, (group) => {
							return openBlock(), createBlock("div", { key: group.category }, [createVNode("h2", { class: "text-[13px] font-bold text-zinc-800 dark:text-zinc-200 mb-3 uppercase tracking-wider flex items-center gap-3" }, [createVNode("span", null, toDisplayString(group.category), 1), createVNode("div", { class: "h-px bg-zinc-200 dark:bg-zinc-800 flex-1 mt-0.5" })]), createVNode("div", { class: "grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 xl:grid-cols-5 gap-3" }, [(openBlock(true), createBlock(Fragment, null, renderList(group.products, (product) => {
								return openBlock(), createBlock("div", {
									key: product.id,
									onClick: ($event) => addToCart(product),
									class: ["group relative flex flex-col bg-white dark:bg-zinc-900 border rounded-lg overflow-hidden cursor-pointer transition-all duration-200", getCartQty(product.id) > 0 ? "border-primary ring-1 ring-primary/20 shadow-sm" : "border-zinc-200 dark:border-zinc-800 hover:border-primary/50 hover:bg-primary/5"]
								}, [
									getCartQty(product.id) > 0 ? (openBlock(), createBlock("div", {
										key: 0,
										class: "absolute top-1.5 right-1.5 z-10 bg-primary text-primary-foreground text-[10px] font-bold min-w-[20px] h-[20px] flex items-center justify-center rounded-full shadow-md border border-primary-foreground/20 px-1"
									}, toDisplayString(getCartQty(product.id)), 1)) : createCommentVNode("", true),
									createVNode("div", { class: ["h-24 bg-zinc-100 dark:bg-zinc-800 relative overflow-hidden flex items-center justify-center shrink-0 border-b border-zinc-100 dark:border-zinc-800", getCartQty(product.id) > 0 ? "opacity-90" : ""] }, [product.imageUrl ? (openBlock(), createBlock("img", {
										key: 0,
										src: product.imageUrl,
										class: "w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
									}, null, 8, ["src"])) : (openBlock(), createBlock("div", {
										key: 1,
										class: "w-full h-full flex items-center justify-center text-2xl font-bold select-none opacity-80",
										style: productAvatarStyle(product.name)
									}, toDisplayString(product.name.charAt(0).toUpperCase()), 5))], 2),
									createVNode("div", { class: "p-2.5 flex flex-col flex-1 justify-between gap-1.5" }, [createVNode("h3", { class: "font-medium text-xs text-zinc-900 dark:text-zinc-100 line-clamp-2 leading-snug group-hover:text-primary transition-colors" }, toDisplayString(product.name), 1), createVNode("div", { class: "flex items-end justify-between mt-auto pt-1" }, [createVNode("span", { class: "text-[9px] text-zinc-400 font-mono" }, toDisplayString(product.sku || ""), 1), createVNode("span", { class: "text-[11px] font-bold text-zinc-800 dark:text-zinc-200" }, toDisplayString(formatCurrency(product.price)), 1)])])
								], 10, ["onClick"]);
							}), 128))])]);
						}), 128))]))]),
						cart.value.length > 0 ? (openBlock(), createBlock("div", {
							key: 0,
							class: "lg:hidden p-3 border-t border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-950 shrink-0 shadow-[0_-4px_10px_rgba(0,0,0,0.05)]"
						}, [createVNode(_sfc_main$2, {
							onClick: ($event) => showCartMobile.value = true,
							class: "w-full h-12 flex justify-between items-center text-[13px] font-bold shadow-md"
						}, {
							default: withCtx(() => [createVNode("div", { class: "flex items-center gap-2" }, [createVNode(unref(ShoppingCart), { class: "h-4 w-4" }), createVNode("span", null, toDisplayString(cart.value.reduce((sum, item) => sum + item.qty, 0)) + " Item", 1)]), createVNode("span", null, toDisplayString(formatCurrency(total.value)), 1)]),
							_: 1
						}, 8, ["onClick"])])) : createCommentVNode("", true)
					], 2), createVNode("div", { class: ["w-full lg:w-[300px] xl:w-[340px] flex-col shrink-0 bg-white dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 rounded-xl shadow-sm lg:h-auto overflow-hidden lg:flex", showCartMobile.value ? "flex flex-1" : "hidden"] }, [
						createVNode("div", { class: "p-3 border-b border-zinc-200 dark:border-zinc-800 shrink-0 flex items-center justify-between" }, [createVNode("div", { class: "flex items-center gap-2" }, [
							createVNode("button", {
								onClick: ($event) => showCartMobile.value = false,
								class: "lg:hidden p-1 -ml-1 mr-1 text-zinc-500 hover:bg-zinc-100 dark:hover:bg-zinc-800 rounded-md transition-colors"
							}, [createVNode(unref(ArrowLeft), { class: "h-5 w-5" })], 8, ["onClick"]),
							createVNode(unref(ShoppingCart), { class: "h-5 w-5 text-primary hidden lg:block" }),
							createVNode("h2", { class: "font-bold text-lg" }, "Pesanan"),
							createVNode("span", { class: "bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-400 text-[10px] font-bold px-2 py-0.5 rounded-full ml-1" }, toDisplayString(cart.value.reduce((sum, item) => sum + item.qty, 0)) + " Item ", 1)
						]), cart.value.length > 0 ? (openBlock(), createBlock("button", {
							key: 0,
							onClick: ($event) => cart.value = [],
							class: "text-[11px] font-medium text-red-500 hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-500/10 px-2 py-1 rounded transition-colors"
						}, " Kosongkan ", 8, ["onClick"])) : createCommentVNode("", true)]),
						createVNode("div", { class: "flex-1 overflow-y-auto p-2 sm:p-3 custom-scrollbar bg-zinc-50/50 dark:bg-zinc-950/50" }, [cart.value.length === 0 ? (openBlock(), createBlock("div", {
							key: 0,
							class: "h-full flex flex-col items-center justify-center text-muted-foreground space-y-3"
						}, [createVNode("div", { class: "w-16 h-16 rounded-full bg-zinc-100 dark:bg-zinc-900 flex items-center justify-center" }, [createVNode(unref(ShoppingCart), { class: "h-8 w-8 opacity-20" })]), createVNode("p", { class: "text-sm" }, "Keranjang masih kosong")])) : (openBlock(), createBlock("div", {
							key: 1,
							class: "space-y-3"
						}, [createVNode(TransitionGroup, { name: "list" }, {
							default: withCtx(() => [(openBlock(true), createBlock(Fragment, null, renderList(cart.value, (item) => {
								return openBlock(), createBlock("div", {
									key: item.id,
									class: "flex flex-col bg-white dark:bg-zinc-900 border border-zinc-100 dark:border-zinc-800/80 p-2 rounded-lg shadow-sm"
								}, [createVNode("div", { class: "flex items-start justify-between gap-1.5" }, [createVNode("div", { class: "flex-1 min-w-0" }, [createVNode("h4", { class: "font-semibold text-[11px] text-zinc-900 dark:text-zinc-100 line-clamp-2 leading-tight" }, toDisplayString(item.name), 1), createVNode("p", { class: "text-[9px] text-primary font-semibold mt-0.5" }, toDisplayString(formatCurrency(item.price)), 1)]), createVNode("button", {
									onClick: ($event) => removeFromCart(item),
									class: "p-1 text-zinc-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-500/10 rounded-md transition-colors shrink-0"
								}, [createVNode(unref(Trash2), { class: "h-3 w-3" })], 8, ["onClick"])]), createVNode("div", { class: "flex items-center justify-between mt-1.5" }, [createVNode("div", { class: "flex items-center bg-zinc-100 dark:bg-zinc-800/80 rounded p-0.5 border border-zinc-200 dark:border-zinc-700/50" }, [
									createVNode("button", {
										onClick: ($event) => decreaseQty(item),
										class: "w-5 h-5 flex items-center justify-center rounded-[3px] hover:bg-white dark:hover:bg-zinc-700 text-zinc-600 dark:text-zinc-300 shadow-sm transition-colors"
									}, [createVNode(unref(Minus), { class: "h-[10px] w-[10px]" })], 8, ["onClick"]),
									createVNode("span", { class: "w-5 text-center text-[10px] font-bold text-zinc-900 dark:text-zinc-100" }, toDisplayString(item.qty), 1),
									createVNode("button", {
										onClick: ($event) => increaseQty(item),
										class: "w-5 h-5 flex items-center justify-center rounded-[3px] hover:bg-white dark:hover:bg-zinc-700 text-zinc-600 dark:text-zinc-300 shadow-sm transition-colors"
									}, [createVNode(unref(Plus), { class: "h-[10px] w-[10px]" })], 8, ["onClick"])
								]), createVNode("span", { class: "font-bold text-[11px] text-zinc-900 dark:text-zinc-100" }, toDisplayString(formatCurrency(item.price * item.qty)), 1)])]);
							}), 128))]),
							_: 1
						})]))]),
						createVNode("div", { class: "p-2 sm:p-3 border-t border-zinc-200 dark:border-zinc-800 shrink-0 bg-zinc-50/50 dark:bg-zinc-900/30" }, [createVNode("div", { class: "space-y-1 mb-2" }, [
							createVNode("div", { class: "flex justify-between text-xs" }, [createVNode("span", { class: "text-zinc-500 font-medium" }, "Subtotal"), createVNode("span", { class: "font-semibold text-zinc-800 dark:text-zinc-200" }, toDisplayString(formatCurrency(subtotal.value)), 1)]),
							createVNode("div", { class: "flex justify-between text-xs" }, [createVNode("span", { class: "text-zinc-500 font-medium" }, "Pajak (11%)"), createVNode("span", { class: "font-semibold text-zinc-800 dark:text-zinc-200" }, toDisplayString(formatCurrency(tax.value)), 1)]),
							createVNode("div", { class: "h-px w-full bg-zinc-200 dark:bg-zinc-800 my-1.5" }),
							createVNode("div", { class: "flex justify-between items-center" }, [createVNode("span", { class: "text-[13px] font-bold text-zinc-900 dark:text-zinc-100" }, "Total"), createVNode("span", { class: "text-lg font-black text-primary" }, toDisplayString(formatCurrency(total.value)), 1)])
						]), createVNode(_sfc_main$2, {
							onClick: checkout,
							class: "w-full h-9 text-[13px] font-bold shadow-md hover:shadow-lg transition-all",
							disabled: cart.value.length === 0
						}, {
							default: withCtx(() => [createVNode(unref(CreditCard), { class: "h-4 w-4 mr-1.5" }), createTextVNode(" Bayar Pesanan ")]),
							_: 1
						}, 8, ["disabled"])])
					], 2)])];
				}),
				_: 1
			}, _parent));
		};
	}
};
var _sfc_setup = _sfc_main.setup;
_sfc_main.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/pages/KasirPage.vue");
	return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
var KasirPage_default = /* @__PURE__ */ _plugin_vue_export_helper_default(_sfc_main, [["__scopeId", "data-v-cb5cef7c"]]);
//#endregion
export { KasirPage_default as default };
