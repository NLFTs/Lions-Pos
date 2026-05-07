import { r as api } from "../main.mjs";
import { t as _plugin_vue_export_helper_default } from "./_plugin-vue_export-helper-DMwexRDj.js";
import { t as _sfc_main$1 } from "./Button-Bj0EF1Kv.js";
import { d as usePermission, t as _sfc_main$2 } from "./AppLayout-D1IhsFmL.js";
import { t as useToast } from "./useToast-BeMK7Zjj.js";
import { t as useConfirm } from "./useConfirm-CPOv5e0B.js";
import { t as _sfc_main$3 } from "./Card-ClMbbMGU.js";
import { t as _sfc_main$4 } from "./CardContent-g9O7qVnh.js";
import { t as _sfc_main$5 } from "./Input-yu8tAo3O.js";
import { n as _sfc_main$7, t as _sfc_main$6 } from "./Alert-DMYknBO3.js";
import { t as _sfc_main$8 } from "./DataTableSearch-DI2aluk6.js";
import { t as _sfc_main$9 } from "./DataTablePagination-CRAPEico.js";
import "./Badge-PdtEYXOU.js";
import { Fragment, Teleport, Transition, computed, createBlock, createCommentVNode, createTextVNode, createVNode, onMounted, openBlock, ref, renderList, resolveDynamicComponent, toDisplayString, unref, useSSRContext, vModelSelect, vModelText, withCtx, withDirectives } from "vue";
import { ssrIncludeBooleanAttr, ssrInterpolate, ssrLooseContain, ssrLooseEqual, ssrRenderAttr, ssrRenderClass, ssrRenderComponent, ssrRenderList, ssrRenderTeleport, ssrRenderVNode } from "vue/server-renderer";
import { ArrowDownLeft, ArrowLeftRight, ArrowUpRight, History, Loader2, Plus, RotateCcw, Settings2, X } from "lucide-vue-next";
//#region src/pages/StockMutationsPage.vue
var _sfc_main = {
	__name: "StockMutationsPage",
	__ssrInlineRender: true,
	setup(__props) {
		const { can } = usePermission();
		const { toast } = useToast();
		const { confirm } = useConfirm();
		const mutations = ref([]);
		const products = ref([]);
		const partners = ref([]);
		const locations = ref([
			{
				id: "wh-1",
				name: "Gudang Utama",
				type: "warehouse"
			},
			{
				id: "br-1",
				name: "Cabang Jakarta",
				type: "branch"
			},
			{
				id: "br-2",
				name: "Cabang Bandung",
				type: "branch"
			}
		]);
		const loading = ref(false);
		const error = ref(null);
		const searchQuery = ref("");
		const page = ref(1);
		const pageSize = ref(10);
		const mutationTypes = [
			{
				value: "sale_out",
				label: "Penjualan (Out)",
				icon: ArrowUpRight,
				color: "text-red-500 bg-red-50"
			},
			{
				value: "purchase_in",
				label: "Pembelian (In)",
				icon: ArrowDownLeft,
				color: "text-emerald-500 bg-emerald-50"
			},
			{
				value: "transfer",
				label: "Transfer Stok",
				icon: ArrowLeftRight,
				color: "text-blue-500 bg-blue-50"
			},
			{
				value: "adjustment",
				label: "Penyesuaian",
				icon: Settings2,
				color: "text-amber-500 bg-amber-50"
			},
			{
				value: "return",
				label: "Retur",
				icon: RotateCcw,
				color: "text-purple-500 bg-purple-50"
			}
		];
		const referenceTypes = [
			{
				value: "order",
				label: "Pesanan (Order)"
			},
			{
				value: "transfer_request",
				label: "Permintaan Transfer"
			},
			{
				value: "stock_opname",
				label: "Stock Opname"
			}
		];
		const showDrawer = ref(false);
		const saving = ref(false);
		const formError = ref(null);
		const emptyForm = () => ({
			id: null,
			partner_id: "",
			product_id: "",
			type: "sale_out",
			from_location_type: "",
			from_location_id: "",
			to_location_type: "",
			to_location_id: "",
			qty: 1,
			reference_type: "",
			reference_id: "",
			notes: ""
		});
		const form = ref(emptyForm());
		const filteredMutations = computed(() => {
			if (!searchQuery.value) return mutations.value;
			const q = searchQuery.value.toLowerCase();
			return mutations.value.filter((m) => m.productName?.toLowerCase().includes(q) || m.notes?.toLowerCase().includes(q) || m.type?.toLowerCase().includes(q));
		});
		const paginatedMutations = computed(() => {
			const start = (page.value - 1) * pageSize.value;
			return filteredMutations.value.slice(start, start + pageSize.value);
		});
		async function fetchMutations() {
			loading.value = true;
			try {
				mutations.value = (await api.get("/api/v1/stock-mutations")).data.data;
			} catch (err) {
				error.value = "Gagal memuat data mutasi.";
			} finally {
				loading.value = false;
			}
		}
		async function fetchProducts() {
			try {
				products.value = (await api.get("/api/v1/products?size=100")).data.data.content;
			} catch (_) {}
		}
		async function fetchPartners() {
			partners.value = [{
				id: "p1",
				name: "Supplier A"
			}, {
				id: "p2",
				name: "Customer B"
			}];
		}
		function openCreate() {
			form.value = emptyForm();
			formError.value = null;
			showDrawer.value = true;
		}
		async function saveMutation() {
			saving.value = true;
			formError.value = null;
			try {
				await api.post("/api/v1/stock-mutations", form.value);
				toast.success("Mutasi stok berhasil dicatat!");
				showDrawer.value = false;
				fetchMutations();
			} catch (err) {
				formError.value = err.response?.data?.message || "Gagal menyimpan mutasi.";
			} finally {
				saving.value = false;
			}
		}
		function getTypeInfo(type) {
			return mutationTypes.find((t) => t.value === type) || mutationTypes[0];
		}
		onMounted(() => {
			fetchMutations();
			fetchProducts();
			fetchPartners();
		});
		return (_ctx, _push, _parent, _attrs) => {
			_push(ssrRenderComponent(_sfc_main$2, _attrs, {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) {
						_push(`<div class="pb-6" data-v-ef49a748${_scopeId}><div class="mb-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4" data-v-ef49a748${_scopeId}><div data-v-ef49a748${_scopeId}><h1 class="text-xl font-bold tracking-tight text-zinc-900 dark:text-zinc-100" data-v-ef49a748${_scopeId}>Mutasi Stok</h1><p class="text-xs text-zinc-500 mt-0.5" data-v-ef49a748${_scopeId}>Pantau dan kelola pergerakan stok barang.</p></div>`);
						_push(ssrRenderComponent(_sfc_main$1, {
							onClick: openCreate,
							class: "bg-primary hover:bg-primary/90 flex items-center gap-2"
						}, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) {
									_push(ssrRenderComponent(unref(Plus), { class: "h-4 w-4" }, null, _parent, _scopeId));
									_push(`<span data-v-ef49a748${_scopeId}>Tambah Mutasi</span>`);
								} else return [createVNode(unref(Plus), { class: "h-4 w-4" }), createVNode("span", null, "Tambah Mutasi")];
							}),
							_: 1
						}, _parent, _scopeId));
						_push(`</div><div class="flex flex-col sm:flex-row items-center justify-between gap-4 mb-5" data-v-ef49a748${_scopeId}>`);
						_push(ssrRenderComponent(_sfc_main$8, {
							modelValue: searchQuery.value,
							"onUpdate:modelValue": ($event) => searchQuery.value = $event,
							placeholder: "Cari mutasi...",
							class: "w-full sm:max-w-sm"
						}, null, _parent, _scopeId));
						_push(`</div>`);
						_push(ssrRenderComponent(_sfc_main$3, null, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) _push(ssrRenderComponent(_sfc_main$4, { class: "p-0" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) {
											if (loading.value) {
												_push(`<div class="flex items-center justify-center py-20" data-v-ef49a748${_scopeId}>`);
												_push(ssrRenderComponent(unref(Loader2), { class: "h-6 w-6 animate-spin text-muted-foreground" }, null, _parent, _scopeId));
												_push(`</div>`);
											} else if (filteredMutations.value.length === 0) {
												_push(`<div class="flex flex-col items-center justify-center py-20 text-muted-foreground" data-v-ef49a748${_scopeId}>`);
												_push(ssrRenderComponent(unref(History), { class: "h-10 w-10 mb-3 opacity-20" }, null, _parent, _scopeId));
												_push(`<p class="text-sm" data-v-ef49a748${_scopeId}>Belum ada data mutasi stok.</p></div>`);
											} else {
												_push(`<div class="overflow-x-auto" data-v-ef49a748${_scopeId}><table class="w-full text-sm" data-v-ef49a748${_scopeId}><thead data-v-ef49a748${_scopeId}><tr class="bg-muted/40 border-b" data-v-ef49a748${_scopeId}><th class="px-4 py-3 text-left font-medium text-muted-foreground" data-v-ef49a748${_scopeId}>Tanggal</th><th class="px-4 py-3 text-left font-medium text-muted-foreground" data-v-ef49a748${_scopeId}>Produk</th><th class="px-4 py-3 text-left font-medium text-muted-foreground" data-v-ef49a748${_scopeId}>Tipe</th><th class="px-4 py-3 text-right font-medium text-muted-foreground" data-v-ef49a748${_scopeId}>Qty</th><th class="px-4 py-3 text-left font-medium text-muted-foreground" data-v-ef49a748${_scopeId}>Keterangan</th><th class="px-4 py-3 text-right font-medium text-muted-foreground" data-v-ef49a748${_scopeId}>Oleh</th></tr></thead><tbody data-v-ef49a748${_scopeId}><!--[-->`);
												ssrRenderList(paginatedMutations.value, (m) => {
													_push(`<tr class="border-b last:border-0 hover:bg-muted/30 transition-colors" data-v-ef49a748${_scopeId}><td class="px-4 py-3 whitespace-nowrap text-xs text-muted-foreground" data-v-ef49a748${_scopeId}>${ssrInterpolate(new Date(m.createdAt).toLocaleString("id-ID"))}</td><td class="px-4 py-3 font-medium" data-v-ef49a748${_scopeId}>${ssrInterpolate(m.productName)}</td><td class="px-4 py-3" data-v-ef49a748${_scopeId}><div class="flex items-center gap-2" data-v-ef49a748${_scopeId}><div class="${ssrRenderClass(["p-1 rounded-md", getTypeInfo(m.type).color])}" data-v-ef49a748${_scopeId}>`);
													ssrRenderVNode(_push, createVNode(resolveDynamicComponent(getTypeInfo(m.type).icon), { class: "h-3 w-3" }, null), _parent, _scopeId);
													_push(`</div><span class="text-xs font-medium" data-v-ef49a748${_scopeId}>${ssrInterpolate(getTypeInfo(m.type).label)}</span></div></td><td class="${ssrRenderClass([m.type.endsWith("_in") ? "text-emerald-600" : "text-red-600", "px-4 py-3 text-right font-semibold"])}" data-v-ef49a748${_scopeId}>${ssrInterpolate(m.type.endsWith("_in") ? "+" : "-")}${ssrInterpolate(m.qty)}</td><td class="px-4 py-3 text-xs text-muted-foreground max-w-[200px] truncate" data-v-ef49a748${_scopeId}>${ssrInterpolate(m.notes || "—")}</td><td class="px-4 py-3 text-right text-xs" data-v-ef49a748${_scopeId}>${ssrInterpolate(m.createdBy)}</td></tr>`);
												});
												_push(`<!--]--></tbody></table></div>`);
											}
											if (filteredMutations.value.length > 0 && !loading.value) _push(ssrRenderComponent(_sfc_main$9, {
												page: page.value,
												"page-size": pageSize.value,
												total: filteredMutations.value.length,
												"onUpdate:page": ($event) => page.value = $event,
												"onUpdate:pageSize": ($event) => {
													pageSize.value = $event;
													page.value = 1;
												}
											}, null, _parent, _scopeId));
											else _push(`<!---->`);
										} else return [loading.value ? (openBlock(), createBlock("div", {
											key: 0,
											class: "flex items-center justify-center py-20"
										}, [createVNode(unref(Loader2), { class: "h-6 w-6 animate-spin text-muted-foreground" })])) : filteredMutations.value.length === 0 ? (openBlock(), createBlock("div", {
											key: 1,
											class: "flex flex-col items-center justify-center py-20 text-muted-foreground"
										}, [createVNode(unref(History), { class: "h-10 w-10 mb-3 opacity-20" }), createVNode("p", { class: "text-sm" }, "Belum ada data mutasi stok.")])) : (openBlock(), createBlock("div", {
											key: 2,
											class: "overflow-x-auto"
										}, [createVNode("table", { class: "w-full text-sm" }, [createVNode("thead", null, [createVNode("tr", { class: "bg-muted/40 border-b" }, [
											createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Tanggal"),
											createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Produk"),
											createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Tipe"),
											createVNode("th", { class: "px-4 py-3 text-right font-medium text-muted-foreground" }, "Qty"),
											createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Keterangan"),
											createVNode("th", { class: "px-4 py-3 text-right font-medium text-muted-foreground" }, "Oleh")
										])]), createVNode("tbody", null, [(openBlock(true), createBlock(Fragment, null, renderList(paginatedMutations.value, (m) => {
											return openBlock(), createBlock("tr", {
												key: m.id,
												class: "border-b last:border-0 hover:bg-muted/30 transition-colors"
											}, [
												createVNode("td", { class: "px-4 py-3 whitespace-nowrap text-xs text-muted-foreground" }, toDisplayString(new Date(m.createdAt).toLocaleString("id-ID")), 1),
												createVNode("td", { class: "px-4 py-3 font-medium" }, toDisplayString(m.productName), 1),
												createVNode("td", { class: "px-4 py-3" }, [createVNode("div", { class: "flex items-center gap-2" }, [createVNode("div", { class: ["p-1 rounded-md", getTypeInfo(m.type).color] }, [(openBlock(), createBlock(resolveDynamicComponent(getTypeInfo(m.type).icon), { class: "h-3 w-3" }))], 2), createVNode("span", { class: "text-xs font-medium" }, toDisplayString(getTypeInfo(m.type).label), 1)])]),
												createVNode("td", { class: ["px-4 py-3 text-right font-semibold", m.type.endsWith("_in") ? "text-emerald-600" : "text-red-600"] }, toDisplayString(m.type.endsWith("_in") ? "+" : "-") + toDisplayString(m.qty), 3),
												createVNode("td", { class: "px-4 py-3 text-xs text-muted-foreground max-w-[200px] truncate" }, toDisplayString(m.notes || "—"), 1),
												createVNode("td", { class: "px-4 py-3 text-right text-xs" }, toDisplayString(m.createdBy), 1)
											]);
										}), 128))])])])), filteredMutations.value.length > 0 && !loading.value ? (openBlock(), createBlock(_sfc_main$9, {
											key: 3,
											page: page.value,
											"page-size": pageSize.value,
											total: filteredMutations.value.length,
											"onUpdate:page": ($event) => page.value = $event,
											"onUpdate:pageSize": ($event) => {
												pageSize.value = $event;
												page.value = 1;
											}
										}, null, 8, [
											"page",
											"page-size",
											"total",
											"onUpdate:page",
											"onUpdate:pageSize"
										])) : createCommentVNode("", true)];
									}),
									_: 1
								}, _parent, _scopeId));
								else return [createVNode(_sfc_main$4, { class: "p-0" }, {
									default: withCtx(() => [loading.value ? (openBlock(), createBlock("div", {
										key: 0,
										class: "flex items-center justify-center py-20"
									}, [createVNode(unref(Loader2), { class: "h-6 w-6 animate-spin text-muted-foreground" })])) : filteredMutations.value.length === 0 ? (openBlock(), createBlock("div", {
										key: 1,
										class: "flex flex-col items-center justify-center py-20 text-muted-foreground"
									}, [createVNode(unref(History), { class: "h-10 w-10 mb-3 opacity-20" }), createVNode("p", { class: "text-sm" }, "Belum ada data mutasi stok.")])) : (openBlock(), createBlock("div", {
										key: 2,
										class: "overflow-x-auto"
									}, [createVNode("table", { class: "w-full text-sm" }, [createVNode("thead", null, [createVNode("tr", { class: "bg-muted/40 border-b" }, [
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Tanggal"),
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Produk"),
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Tipe"),
										createVNode("th", { class: "px-4 py-3 text-right font-medium text-muted-foreground" }, "Qty"),
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Keterangan"),
										createVNode("th", { class: "px-4 py-3 text-right font-medium text-muted-foreground" }, "Oleh")
									])]), createVNode("tbody", null, [(openBlock(true), createBlock(Fragment, null, renderList(paginatedMutations.value, (m) => {
										return openBlock(), createBlock("tr", {
											key: m.id,
											class: "border-b last:border-0 hover:bg-muted/30 transition-colors"
										}, [
											createVNode("td", { class: "px-4 py-3 whitespace-nowrap text-xs text-muted-foreground" }, toDisplayString(new Date(m.createdAt).toLocaleString("id-ID")), 1),
											createVNode("td", { class: "px-4 py-3 font-medium" }, toDisplayString(m.productName), 1),
											createVNode("td", { class: "px-4 py-3" }, [createVNode("div", { class: "flex items-center gap-2" }, [createVNode("div", { class: ["p-1 rounded-md", getTypeInfo(m.type).color] }, [(openBlock(), createBlock(resolveDynamicComponent(getTypeInfo(m.type).icon), { class: "h-3 w-3" }))], 2), createVNode("span", { class: "text-xs font-medium" }, toDisplayString(getTypeInfo(m.type).label), 1)])]),
											createVNode("td", { class: ["px-4 py-3 text-right font-semibold", m.type.endsWith("_in") ? "text-emerald-600" : "text-red-600"] }, toDisplayString(m.type.endsWith("_in") ? "+" : "-") + toDisplayString(m.qty), 3),
											createVNode("td", { class: "px-4 py-3 text-xs text-muted-foreground max-w-[200px] truncate" }, toDisplayString(m.notes || "—"), 1),
											createVNode("td", { class: "px-4 py-3 text-right text-xs" }, toDisplayString(m.createdBy), 1)
										]);
									}), 128))])])])), filteredMutations.value.length > 0 && !loading.value ? (openBlock(), createBlock(_sfc_main$9, {
										key: 3,
										page: page.value,
										"page-size": pageSize.value,
										total: filteredMutations.value.length,
										"onUpdate:page": ($event) => page.value = $event,
										"onUpdate:pageSize": ($event) => {
											pageSize.value = $event;
											page.value = 1;
										}
									}, null, 8, [
										"page",
										"page-size",
										"total",
										"onUpdate:page",
										"onUpdate:pageSize"
									])) : createCommentVNode("", true)]),
									_: 2
								}, 1024)];
							}),
							_: 1
						}, _parent, _scopeId));
						_push(`</div>`);
						ssrRenderTeleport(_push, (_push) => {
							if (showDrawer.value) _push(`<div class="fixed inset-0 z-[100] bg-black/40 backdrop-blur-sm" data-v-ef49a748${_scopeId}></div>`);
							else _push(`<!---->`);
							if (showDrawer.value) {
								_push(`<div class="fixed inset-y-0 right-0 z-[101] flex flex-col w-full sm:max-w-[480px] h-full bg-card shadow-2xl sm:border-l overflow-hidden" data-v-ef49a748${_scopeId}><div class="flex items-center justify-between px-6 py-4 border-b shrink-0" data-v-ef49a748${_scopeId}><div data-v-ef49a748${_scopeId}><h3 class="font-semibold text-base" data-v-ef49a748${_scopeId}>Tambah Mutasi Stok</h3><p class="text-xs text-muted-foreground mt-0.5" data-v-ef49a748${_scopeId}>Catat pergerakan stok baru.</p></div>`);
								_push(ssrRenderComponent(_sfc_main$1, {
									variant: "ghost",
									size: "icon",
									onClick: ($event) => showDrawer.value = false
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(ssrRenderComponent(unref(X), { class: "h-4 w-4" }, null, _parent, _scopeId));
										else return [createVNode(unref(X), { class: "h-4 w-4" })];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`</div><div class="flex-1 overflow-y-auto px-6 py-5 space-y-5" data-v-ef49a748${_scopeId}>`);
								if (formError.value) _push(ssrRenderComponent(_sfc_main$6, { variant: "destructive" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`${ssrInterpolate(formError.value)}`);
										else return [createTextVNode(toDisplayString(formError.value), 1)];
									}),
									_: 1
								}, _parent, _scopeId));
								else _push(`<!---->`);
								_push(`<div class="space-y-1.5" data-v-ef49a748${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, null, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Tipe Mutasi <span class="text-destructive" data-v-ef49a748${_scopeId}>*</span>`);
										else return [createTextVNode("Tipe Mutasi "), createVNode("span", { class: "text-destructive" }, "*")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`<div class="grid grid-cols-2 gap-2" data-v-ef49a748${_scopeId}><!--[-->`);
								ssrRenderList(mutationTypes, (t) => {
									_push(`<button class="${ssrRenderClass([form.value.type === t.value ? "border-primary bg-primary/5 text-primary ring-1 ring-primary" : "border-zinc-200 dark:border-zinc-800 hover:bg-zinc-50 dark:hover:bg-zinc-900", "flex flex-col items-center gap-2 p-3 rounded-lg border transition-all text-center"])}" data-v-ef49a748${_scopeId}>`);
									ssrRenderVNode(_push, createVNode(resolveDynamicComponent(t.icon), { class: "h-5 w-5" }, null), _parent, _scopeId);
									_push(`<span class="text-[10px] font-semibold leading-tight" data-v-ef49a748${_scopeId}>${ssrInterpolate(t.label)}</span></button>`);
								});
								_push(`<!--]--></div></div><div class="space-y-1.5" data-v-ef49a748${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, { for: "product" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Produk <span class="text-destructive" data-v-ef49a748${_scopeId}>*</span>`);
										else return [createTextVNode("Produk "), createVNode("span", { class: "text-destructive" }, "*")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`<select id="product" class="w-full h-10 rounded-md border border-input bg-background px-3 text-sm focus:outline-none focus:ring-2 focus:ring-ring" data-v-ef49a748${_scopeId}><option value="" disabled data-v-ef49a748${ssrIncludeBooleanAttr(Array.isArray(form.value.product_id) ? ssrLooseContain(form.value.product_id, "") : ssrLooseEqual(form.value.product_id, "")) ? " selected" : ""}${_scopeId}>Pilih Produk...</option><!--[-->`);
								ssrRenderList(products.value, (p) => {
									_push(`<option${ssrRenderAttr("value", p.id)} data-v-ef49a748${ssrIncludeBooleanAttr(Array.isArray(form.value.product_id) ? ssrLooseContain(form.value.product_id, p.id) : ssrLooseEqual(form.value.product_id, p.id)) ? " selected" : ""}${_scopeId}>${ssrInterpolate(p.name)} (${ssrInterpolate(p.sku)})</option>`);
								});
								_push(`<!--]--></select></div><div class="grid grid-cols-2 gap-4" data-v-ef49a748${_scopeId}><div class="space-y-1.5" data-v-ef49a748${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, { for: "partner" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Partner (Supplier/Cust)`);
										else return [createTextVNode("Partner (Supplier/Cust)")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`<select id="partner" class="w-full h-10 rounded-md border border-input bg-background px-3 text-sm" data-v-ef49a748${_scopeId}><option value="" data-v-ef49a748${ssrIncludeBooleanAttr(Array.isArray(form.value.partner_id) ? ssrLooseContain(form.value.partner_id, "") : ssrLooseEqual(form.value.partner_id, "")) ? " selected" : ""}${_scopeId}>None</option><!--[-->`);
								ssrRenderList(partners.value, (p) => {
									_push(`<option${ssrRenderAttr("value", p.id)} data-v-ef49a748${ssrIncludeBooleanAttr(Array.isArray(form.value.partner_id) ? ssrLooseContain(form.value.partner_id, p.id) : ssrLooseEqual(form.value.partner_id, p.id)) ? " selected" : ""}${_scopeId}>${ssrInterpolate(p.name)}</option>`);
								});
								_push(`<!--]--></select></div><div class="space-y-1.5" data-v-ef49a748${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, { for: "qty" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Jumlah <span class="text-destructive" data-v-ef49a748${_scopeId}>*</span>`);
										else return [createTextVNode("Jumlah "), createVNode("span", { class: "text-destructive" }, "*")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$5, {
									id: "qty",
									type: "number",
									modelValue: form.value.qty,
									"onUpdate:modelValue": ($event) => form.value.qty = $event,
									min: "1",
									step: "0.01"
								}, null, _parent, _scopeId));
								_push(`</div></div><hr class="border-zinc-100 dark:border-zinc-800" data-v-ef49a748${_scopeId}><div class="space-y-4" data-v-ef49a748${_scopeId}><div class="grid grid-cols-2 gap-4" data-v-ef49a748${_scopeId}><div class="space-y-1.5" data-v-ef49a748${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, null, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Asal (From)`);
										else return [createTextVNode("Asal (From)")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`<select class="w-full h-10 rounded-md border border-input bg-background px-3 text-sm" data-v-ef49a748${_scopeId}><option value="" data-v-ef49a748${ssrIncludeBooleanAttr(Array.isArray(form.value.from_location_id) ? ssrLooseContain(form.value.from_location_id, "") : ssrLooseEqual(form.value.from_location_id, "")) ? " selected" : ""}${_scopeId}>None</option><!--[-->`);
								ssrRenderList(locations.value, (l) => {
									_push(`<option${ssrRenderAttr("value", l.id)} data-v-ef49a748${ssrIncludeBooleanAttr(Array.isArray(form.value.from_location_id) ? ssrLooseContain(form.value.from_location_id, l.id) : ssrLooseEqual(form.value.from_location_id, l.id)) ? " selected" : ""}${_scopeId}>[${ssrInterpolate(l.type)}] ${ssrInterpolate(l.name)}</option>`);
								});
								_push(`<!--]--></select></div><div class="space-y-1.5" data-v-ef49a748${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, null, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Tujuan (To)`);
										else return [createTextVNode("Tujuan (To)")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`<select class="w-full h-10 rounded-md border border-input bg-background px-3 text-sm" data-v-ef49a748${_scopeId}><option value="" data-v-ef49a748${ssrIncludeBooleanAttr(Array.isArray(form.value.to_location_id) ? ssrLooseContain(form.value.to_location_id, "") : ssrLooseEqual(form.value.to_location_id, "")) ? " selected" : ""}${_scopeId}>None</option><!--[-->`);
								ssrRenderList(locations.value, (l) => {
									_push(`<option${ssrRenderAttr("value", l.id)} data-v-ef49a748${ssrIncludeBooleanAttr(Array.isArray(form.value.to_location_id) ? ssrLooseContain(form.value.to_location_id, l.id) : ssrLooseEqual(form.value.to_location_id, l.id)) ? " selected" : ""}${_scopeId}>[${ssrInterpolate(l.type)}] ${ssrInterpolate(l.name)}</option>`);
								});
								_push(`<!--]--></select></div></div></div><div class="grid grid-cols-2 gap-4" data-v-ef49a748${_scopeId}><div class="space-y-1.5" data-v-ef49a748${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, null, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Tipe Referensi`);
										else return [createTextVNode("Tipe Referensi")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`<select class="w-full h-10 rounded-md border border-input bg-background px-3 text-sm" data-v-ef49a748${_scopeId}><option value="" data-v-ef49a748${ssrIncludeBooleanAttr(Array.isArray(form.value.reference_type) ? ssrLooseContain(form.value.reference_type, "") : ssrLooseEqual(form.value.reference_type, "")) ? " selected" : ""}${_scopeId}>None</option><!--[-->`);
								ssrRenderList(referenceTypes, (r) => {
									_push(`<option${ssrRenderAttr("value", r.value)} data-v-ef49a748${ssrIncludeBooleanAttr(Array.isArray(form.value.reference_type) ? ssrLooseContain(form.value.reference_type, r.value) : ssrLooseEqual(form.value.reference_type, r.value)) ? " selected" : ""}${_scopeId}>${ssrInterpolate(r.label)}</option>`);
								});
								_push(`<!--]--></select></div><div class="space-y-1.5" data-v-ef49a748${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, null, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`ID Referensi`);
										else return [createTextVNode("ID Referensi")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$5, {
									modelValue: form.value.reference_id,
									"onUpdate:modelValue": ($event) => form.value.reference_id = $event,
									placeholder: "ID order/req..."
								}, null, _parent, _scopeId));
								_push(`</div></div><div class="space-y-1.5" data-v-ef49a748${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, { for: "notes" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Catatan`);
										else return [createTextVNode("Catatan")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`<textarea id="notes" rows="3" class="w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring" placeholder="Tambahkan catatan mutasi..." data-v-ef49a748${_scopeId}>${ssrInterpolate(form.value.notes)}</textarea></div></div><div class="flex justify-end gap-3 px-6 py-4 border-t bg-muted/30" data-v-ef49a748${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$1, {
									variant: "outline",
									onClick: ($event) => showDrawer.value = false,
									disabled: saving.value
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Batal`);
										else return [createTextVNode("Batal")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$1, {
									onClick: saveMutation,
									disabled: saving.value
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) {
											if (saving.value) _push(ssrRenderComponent(unref(Loader2), { class: "h-4 w-4 mr-2 animate-spin" }, null, _parent, _scopeId));
											else _push(`<!---->`);
											_push(` Simpan Mutasi `);
										} else return [saving.value ? (openBlock(), createBlock(unref(Loader2), {
											key: 0,
											class: "h-4 w-4 mr-2 animate-spin"
										})) : createCommentVNode("", true), createTextVNode(" Simpan Mutasi ")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`</div></div>`);
							} else _push(`<!---->`);
						}, "body", false, _parent);
					} else return [createVNode("div", { class: "pb-6" }, [
						createVNode("div", { class: "mb-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4" }, [createVNode("div", null, [createVNode("h1", { class: "text-xl font-bold tracking-tight text-zinc-900 dark:text-zinc-100" }, "Mutasi Stok"), createVNode("p", { class: "text-xs text-zinc-500 mt-0.5" }, "Pantau dan kelola pergerakan stok barang.")]), createVNode(_sfc_main$1, {
							onClick: openCreate,
							class: "bg-primary hover:bg-primary/90 flex items-center gap-2"
						}, {
							default: withCtx(() => [createVNode(unref(Plus), { class: "h-4 w-4" }), createVNode("span", null, "Tambah Mutasi")]),
							_: 1
						})]),
						createVNode("div", { class: "flex flex-col sm:flex-row items-center justify-between gap-4 mb-5" }, [createVNode(_sfc_main$8, {
							modelValue: searchQuery.value,
							"onUpdate:modelValue": ($event) => searchQuery.value = $event,
							placeholder: "Cari mutasi...",
							class: "w-full sm:max-w-sm"
						}, null, 8, ["modelValue", "onUpdate:modelValue"])]),
						createVNode(_sfc_main$3, null, {
							default: withCtx(() => [createVNode(_sfc_main$4, { class: "p-0" }, {
								default: withCtx(() => [loading.value ? (openBlock(), createBlock("div", {
									key: 0,
									class: "flex items-center justify-center py-20"
								}, [createVNode(unref(Loader2), { class: "h-6 w-6 animate-spin text-muted-foreground" })])) : filteredMutations.value.length === 0 ? (openBlock(), createBlock("div", {
									key: 1,
									class: "flex flex-col items-center justify-center py-20 text-muted-foreground"
								}, [createVNode(unref(History), { class: "h-10 w-10 mb-3 opacity-20" }), createVNode("p", { class: "text-sm" }, "Belum ada data mutasi stok.")])) : (openBlock(), createBlock("div", {
									key: 2,
									class: "overflow-x-auto"
								}, [createVNode("table", { class: "w-full text-sm" }, [createVNode("thead", null, [createVNode("tr", { class: "bg-muted/40 border-b" }, [
									createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Tanggal"),
									createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Produk"),
									createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Tipe"),
									createVNode("th", { class: "px-4 py-3 text-right font-medium text-muted-foreground" }, "Qty"),
									createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Keterangan"),
									createVNode("th", { class: "px-4 py-3 text-right font-medium text-muted-foreground" }, "Oleh")
								])]), createVNode("tbody", null, [(openBlock(true), createBlock(Fragment, null, renderList(paginatedMutations.value, (m) => {
									return openBlock(), createBlock("tr", {
										key: m.id,
										class: "border-b last:border-0 hover:bg-muted/30 transition-colors"
									}, [
										createVNode("td", { class: "px-4 py-3 whitespace-nowrap text-xs text-muted-foreground" }, toDisplayString(new Date(m.createdAt).toLocaleString("id-ID")), 1),
										createVNode("td", { class: "px-4 py-3 font-medium" }, toDisplayString(m.productName), 1),
										createVNode("td", { class: "px-4 py-3" }, [createVNode("div", { class: "flex items-center gap-2" }, [createVNode("div", { class: ["p-1 rounded-md", getTypeInfo(m.type).color] }, [(openBlock(), createBlock(resolveDynamicComponent(getTypeInfo(m.type).icon), { class: "h-3 w-3" }))], 2), createVNode("span", { class: "text-xs font-medium" }, toDisplayString(getTypeInfo(m.type).label), 1)])]),
										createVNode("td", { class: ["px-4 py-3 text-right font-semibold", m.type.endsWith("_in") ? "text-emerald-600" : "text-red-600"] }, toDisplayString(m.type.endsWith("_in") ? "+" : "-") + toDisplayString(m.qty), 3),
										createVNode("td", { class: "px-4 py-3 text-xs text-muted-foreground max-w-[200px] truncate" }, toDisplayString(m.notes || "—"), 1),
										createVNode("td", { class: "px-4 py-3 text-right text-xs" }, toDisplayString(m.createdBy), 1)
									]);
								}), 128))])])])), filteredMutations.value.length > 0 && !loading.value ? (openBlock(), createBlock(_sfc_main$9, {
									key: 3,
									page: page.value,
									"page-size": pageSize.value,
									total: filteredMutations.value.length,
									"onUpdate:page": ($event) => page.value = $event,
									"onUpdate:pageSize": ($event) => {
										pageSize.value = $event;
										page.value = 1;
									}
								}, null, 8, [
									"page",
									"page-size",
									"total",
									"onUpdate:page",
									"onUpdate:pageSize"
								])) : createCommentVNode("", true)]),
								_: 2
							}, 1024)]),
							_: 2
						}, 1024)
					]), (openBlock(), createBlock(Teleport, { to: "body" }, [createVNode(Transition, { name: "fade" }, {
						default: withCtx(() => [showDrawer.value ? (openBlock(), createBlock("div", {
							key: 0,
							class: "fixed inset-0 z-[100] bg-black/40 backdrop-blur-sm",
							onClick: ($event) => showDrawer.value = false
						}, null, 8, ["onClick"])) : createCommentVNode("", true)]),
						_: 1
					}), createVNode(Transition, { name: "slide-right" }, {
						default: withCtx(() => [showDrawer.value ? (openBlock(), createBlock("div", {
							key: 0,
							class: "fixed inset-y-0 right-0 z-[101] flex flex-col w-full sm:max-w-[480px] h-full bg-card shadow-2xl sm:border-l overflow-hidden"
						}, [
							createVNode("div", { class: "flex items-center justify-between px-6 py-4 border-b shrink-0" }, [createVNode("div", null, [createVNode("h3", { class: "font-semibold text-base" }, "Tambah Mutasi Stok"), createVNode("p", { class: "text-xs text-muted-foreground mt-0.5" }, "Catat pergerakan stok baru.")]), createVNode(_sfc_main$1, {
								variant: "ghost",
								size: "icon",
								onClick: ($event) => showDrawer.value = false
							}, {
								default: withCtx(() => [createVNode(unref(X), { class: "h-4 w-4" })]),
								_: 1
							}, 8, ["onClick"])]),
							createVNode("div", { class: "flex-1 overflow-y-auto px-6 py-5 space-y-5" }, [
								formError.value ? (openBlock(), createBlock(_sfc_main$6, {
									key: 0,
									variant: "destructive"
								}, {
									default: withCtx(() => [createTextVNode(toDisplayString(formError.value), 1)]),
									_: 1
								})) : createCommentVNode("", true),
								createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, null, {
									default: withCtx(() => [createTextVNode("Tipe Mutasi "), createVNode("span", { class: "text-destructive" }, "*")]),
									_: 1
								}), createVNode("div", { class: "grid grid-cols-2 gap-2" }, [(openBlock(), createBlock(Fragment, null, renderList(mutationTypes, (t) => {
									return createVNode("button", {
										key: t.value,
										onClick: ($event) => form.value.type = t.value,
										class: ["flex flex-col items-center gap-2 p-3 rounded-lg border transition-all text-center", form.value.type === t.value ? "border-primary bg-primary/5 text-primary ring-1 ring-primary" : "border-zinc-200 dark:border-zinc-800 hover:bg-zinc-50 dark:hover:bg-zinc-900"]
									}, [(openBlock(), createBlock(resolveDynamicComponent(t.icon), { class: "h-5 w-5" })), createVNode("span", { class: "text-[10px] font-semibold leading-tight" }, toDisplayString(t.label), 1)], 10, ["onClick"]);
								}), 64))])]),
								createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, { for: "product" }, {
									default: withCtx(() => [createTextVNode("Produk "), createVNode("span", { class: "text-destructive" }, "*")]),
									_: 1
								}), withDirectives(createVNode("select", {
									id: "product",
									"onUpdate:modelValue": ($event) => form.value.product_id = $event,
									class: "w-full h-10 rounded-md border border-input bg-background px-3 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
								}, [createVNode("option", {
									value: "",
									disabled: ""
								}, "Pilih Produk..."), (openBlock(true), createBlock(Fragment, null, renderList(products.value, (p) => {
									return openBlock(), createBlock("option", {
										key: p.id,
										value: p.id
									}, toDisplayString(p.name) + " (" + toDisplayString(p.sku) + ")", 9, ["value"]);
								}), 128))], 8, ["onUpdate:modelValue"]), [[vModelSelect, form.value.product_id]])]),
								createVNode("div", { class: "grid grid-cols-2 gap-4" }, [createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, { for: "partner" }, {
									default: withCtx(() => [createTextVNode("Partner (Supplier/Cust)")]),
									_: 1
								}), withDirectives(createVNode("select", {
									id: "partner",
									"onUpdate:modelValue": ($event) => form.value.partner_id = $event,
									class: "w-full h-10 rounded-md border border-input bg-background px-3 text-sm"
								}, [createVNode("option", { value: "" }, "None"), (openBlock(true), createBlock(Fragment, null, renderList(partners.value, (p) => {
									return openBlock(), createBlock("option", {
										key: p.id,
										value: p.id
									}, toDisplayString(p.name), 9, ["value"]);
								}), 128))], 8, ["onUpdate:modelValue"]), [[vModelSelect, form.value.partner_id]])]), createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, { for: "qty" }, {
									default: withCtx(() => [createTextVNode("Jumlah "), createVNode("span", { class: "text-destructive" }, "*")]),
									_: 1
								}), createVNode(_sfc_main$5, {
									id: "qty",
									type: "number",
									modelValue: form.value.qty,
									"onUpdate:modelValue": ($event) => form.value.qty = $event,
									min: "1",
									step: "0.01"
								}, null, 8, ["modelValue", "onUpdate:modelValue"])])]),
								createVNode("hr", { class: "border-zinc-100 dark:border-zinc-800" }),
								createVNode("div", { class: "space-y-4" }, [createVNode("div", { class: "grid grid-cols-2 gap-4" }, [createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, null, {
									default: withCtx(() => [createTextVNode("Asal (From)")]),
									_: 1
								}), withDirectives(createVNode("select", {
									"onUpdate:modelValue": ($event) => form.value.from_location_id = $event,
									class: "w-full h-10 rounded-md border border-input bg-background px-3 text-sm"
								}, [createVNode("option", { value: "" }, "None"), (openBlock(true), createBlock(Fragment, null, renderList(locations.value, (l) => {
									return openBlock(), createBlock("option", {
										key: l.id,
										value: l.id
									}, "[" + toDisplayString(l.type) + "] " + toDisplayString(l.name), 9, ["value"]);
								}), 128))], 8, ["onUpdate:modelValue"]), [[vModelSelect, form.value.from_location_id]])]), createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, null, {
									default: withCtx(() => [createTextVNode("Tujuan (To)")]),
									_: 1
								}), withDirectives(createVNode("select", {
									"onUpdate:modelValue": ($event) => form.value.to_location_id = $event,
									class: "w-full h-10 rounded-md border border-input bg-background px-3 text-sm"
								}, [createVNode("option", { value: "" }, "None"), (openBlock(true), createBlock(Fragment, null, renderList(locations.value, (l) => {
									return openBlock(), createBlock("option", {
										key: l.id,
										value: l.id
									}, "[" + toDisplayString(l.type) + "] " + toDisplayString(l.name), 9, ["value"]);
								}), 128))], 8, ["onUpdate:modelValue"]), [[vModelSelect, form.value.to_location_id]])])])]),
								createVNode("div", { class: "grid grid-cols-2 gap-4" }, [createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, null, {
									default: withCtx(() => [createTextVNode("Tipe Referensi")]),
									_: 1
								}), withDirectives(createVNode("select", {
									"onUpdate:modelValue": ($event) => form.value.reference_type = $event,
									class: "w-full h-10 rounded-md border border-input bg-background px-3 text-sm"
								}, [createVNode("option", { value: "" }, "None"), (openBlock(), createBlock(Fragment, null, renderList(referenceTypes, (r) => {
									return createVNode("option", {
										key: r.value,
										value: r.value
									}, toDisplayString(r.label), 9, ["value"]);
								}), 64))], 8, ["onUpdate:modelValue"]), [[vModelSelect, form.value.reference_type]])]), createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, null, {
									default: withCtx(() => [createTextVNode("ID Referensi")]),
									_: 1
								}), createVNode(_sfc_main$5, {
									modelValue: form.value.reference_id,
									"onUpdate:modelValue": ($event) => form.value.reference_id = $event,
									placeholder: "ID order/req..."
								}, null, 8, ["modelValue", "onUpdate:modelValue"])])]),
								createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, { for: "notes" }, {
									default: withCtx(() => [createTextVNode("Catatan")]),
									_: 1
								}), withDirectives(createVNode("textarea", {
									id: "notes",
									"onUpdate:modelValue": ($event) => form.value.notes = $event,
									rows: "3",
									class: "w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring",
									placeholder: "Tambahkan catatan mutasi..."
								}, null, 8, ["onUpdate:modelValue"]), [[vModelText, form.value.notes]])])
							]),
							createVNode("div", { class: "flex justify-end gap-3 px-6 py-4 border-t bg-muted/30" }, [createVNode(_sfc_main$1, {
								variant: "outline",
								onClick: ($event) => showDrawer.value = false,
								disabled: saving.value
							}, {
								default: withCtx(() => [createTextVNode("Batal")]),
								_: 1
							}, 8, ["onClick", "disabled"]), createVNode(_sfc_main$1, {
								onClick: saveMutation,
								disabled: saving.value
							}, {
								default: withCtx(() => [saving.value ? (openBlock(), createBlock(unref(Loader2), {
									key: 0,
									class: "h-4 w-4 mr-2 animate-spin"
								})) : createCommentVNode("", true), createTextVNode(" Simpan Mutasi ")]),
								_: 1
							}, 8, ["disabled"])])
						])) : createCommentVNode("", true)]),
						_: 1
					})]))];
				}),
				_: 1
			}, _parent));
		};
	}
};
var _sfc_setup = _sfc_main.setup;
_sfc_main.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/pages/StockMutationsPage.vue");
	return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
var StockMutationsPage_default = /* @__PURE__ */ _plugin_vue_export_helper_default(_sfc_main, [["__scopeId", "data-v-ef49a748"]]);
//#endregion
export { StockMutationsPage_default as default };
