import { r as api } from "../main.mjs";
import { t as _plugin_vue_export_helper_default } from "./_plugin-vue_export-helper-DMwexRDj.js";
import { n as cn } from "./Button-Bj0EF1Kv.js";
import { d as usePermission, s as _sfc_main$3, t as _sfc_main$2 } from "./AppLayout-D1IhsFmL.js";
import { t as useToast } from "./useToast-BeMK7Zjj.js";
import { t as useConfirm } from "./useConfirm-CPOv5e0B.js";
import { t as _sfc_main$4 } from "./Card-ClMbbMGU.js";
import { t as _sfc_main$5 } from "./CardContent-g9O7qVnh.js";
import { t as _sfc_main$6 } from "./Input-yu8tAo3O.js";
import { n as _sfc_main$8, t as _sfc_main$7 } from "./Alert-DMYknBO3.js";
import { t as _sfc_main$9 } from "./DataTableSearch-DI2aluk6.js";
import { t as _sfc_main$10 } from "./DataTablePagination-CRAPEico.js";
import { a as _sfc_main$12, i as _sfc_main$15, n as _sfc_main$14, o as _sfc_main$11, r as _sfc_main$13, t as _sfc_main$16 } from "./TableCell-DkUauQ5R.js";
import { Fragment, Teleport, Transition, computed, createBlock, createCommentVNode, createTextVNode, createVNode, mergeProps, onBeforeUnmount, onMounted, openBlock, ref, renderList, toDisplayString, unref, useSSRContext, vModelSelect, withCtx, withDirectives } from "vue";
import { ssrIncludeBooleanAttr, ssrInterpolate, ssrLooseContain, ssrLooseEqual, ssrRenderAttr, ssrRenderAttrs, ssrRenderClass, ssrRenderComponent, ssrRenderList, ssrRenderSlot, ssrRenderStyle, ssrRenderTeleport } from "vue/server-renderer";
import { ChevronDown, Filter, Loader2, Package, Pencil, Plus, Trash2, Upload, X } from "lucide-vue-next";
import { cva } from "class-variance-authority";
//#region src/components/ui/badge/index.js
var badgeVariants = cva("inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2", {
	variants: { variant: {
		default: "border-transparent bg-primary text-primary-foreground hover:bg-primary/80",
		secondary: "border-transparent bg-secondary text-secondary-foreground hover:bg-secondary/80",
		destructive: "border-transparent bg-destructive text-destructive-foreground hover:bg-destructive/80",
		outline: "text-foreground",
		success: "border-transparent bg-emerald-500 text-white hover:bg-emerald-500/80",
		warning: "border-transparent bg-amber-500 text-white hover:bg-amber-500/80"
	} },
	defaultVariants: { variant: "default" }
});
//#endregion
//#region src/components/ui/badge/Badge.vue
var _sfc_main$1 = {
	__name: "Badge",
	__ssrInlineRender: true,
	props: {
		variant: {
			type: String,
			default: "default"
		},
		class: {
			type: String,
			default: ""
		}
	},
	setup(__props) {
		const props = __props;
		const delegatedProps = computed(() => {
			const { class: _, ...rest } = props;
			return rest;
		});
		return (_ctx, _push, _parent, _attrs) => {
			_push(`<div${ssrRenderAttrs(mergeProps(delegatedProps.value, { class: unref(cn)(unref(badgeVariants)({ variant: __props.variant }), props.class) }, _attrs))}>`);
			ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent);
			_push(`</div>`);
		};
	}
};
var _sfc_setup$1 = _sfc_main$1.setup;
_sfc_main$1.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/badge/Badge.vue");
	return _sfc_setup$1 ? _sfc_setup$1(props, ctx) : void 0;
};
//#endregion
//#region src/pages/ProductPage.vue
var _sfc_main = {
	__name: "ProductPage",
	__ssrInlineRender: true,
	setup(__props) {
		const { can } = usePermission();
		const { toast } = useToast();
		const { confirm } = useConfirm();
		const products = ref([]);
		const categories = ref([]);
		const pagination = ref({
			page: 0,
			size: 10,
			totalPages: 0,
			totalElements: 0
		});
		const loading = ref(false);
		const error = ref(null);
		const searchQuery = ref("");
		const showFilter = ref(false);
		const sortBy = ref("terbaru");
		const filterStatus = ref("all");
		const filterStock = ref("all");
		const activeFilterCount = computed(() => {
			let count = 0;
			if (sortBy.value !== "terbaru") count++;
			if (filterStatus.value !== "all") count++;
			if (filterStock.value !== "all") count++;
			return count;
		});
		function clearFilters() {
			sortBy.value = "terbaru";
			filterStatus.value = "all";
			filterStock.value = "all";
		}
		const filterRef = ref(null);
		function handleOutsideClick(e) {
			if (filterRef.value && !filterRef.value.contains(e.target)) showFilter.value = false;
		}
		const filteredProducts = computed(() => {
			let result = products.value;
			if (filterStatus.value === "aktif") result = result.filter((p) => p.isActive);
			else if (filterStatus.value === "nonaktif") result = result.filter((p) => !p.isActive);
			if (filterStock.value === "dilacak") result = result.filter((p) => p.trackStock);
			else if (filterStock.value === "bebas") result = result.filter((p) => !p.trackStock);
			if (searchQuery.value) {
				const q = searchQuery.value.toLowerCase();
				result = result.filter((p) => p.name && p.name.toLowerCase().includes(q) || p.sku && p.sku.toLowerCase().includes(q) || p.categoryName && p.categoryName.toLowerCase().includes(q));
			}
			if (sortBy.value === "harga-termahal") result = [...result].sort((a, b) => b.price - a.price);
			else if (sortBy.value === "harga-termurah") result = [...result].sort((a, b) => a.price - b.price);
			else if (sortBy.value === "terbaru") result = [...result].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
			return result;
		});
		const showDrawer = ref(false);
		const modalMode = ref("create");
		const saving = ref(false);
		const formError = ref(null);
		const emptyForm = () => ({
			id: null,
			name: "",
			sku: "",
			price: "",
			categoryId: null,
			trackStock: true,
			isActive: true,
			image: null,
			imagePreview: null
		});
		const form = ref(emptyForm());
		async function fetchProducts(page = 0) {
			loading.value = true;
			error.value = null;
			try {
				const data = (await api.get(`/api/v1/products?page=${page}&size=${pagination.value.size}`)).data.data;
				products.value = data.content;
				pagination.value = {
					...pagination.value,
					page: data.number,
					totalPages: data.totalPages,
					totalElements: data.totalElements
				};
			} catch (err) {
				error.value = err.response?.data?.message || "Gagal memuat data produk.";
			} finally {
				loading.value = false;
			}
		}
		function updatePageSize(newSize) {
			pagination.value.size = newSize;
			fetchProducts(0);
		}
		async function fetchCategories() {
			try {
				categories.value = (await api.get("/api/v1/categories")).data.data;
			} catch (_) {}
		}
		onMounted(() => {
			fetchProducts();
			fetchCategories();
			document.addEventListener("click", handleOutsideClick);
		});
		onBeforeUnmount(() => {
			document.removeEventListener("click", handleOutsideClick);
		});
		function openCreate() {
			form.value = emptyForm();
			formError.value = null;
			modalMode.value = "create";
			showDrawer.value = true;
		}
		function openEdit(product) {
			form.value = {
				id: product.id,
				name: product.name || "",
				sku: product.sku || "",
				price: product.price ?? "",
				categoryId: product.categoryId || null,
				trackStock: product.trackStock ?? true,
				isActive: product.isActive ?? true,
				image: null,
				imagePreview: product.imageUrl || null
			};
			formError.value = null;
			modalMode.value = "edit";
			showDrawer.value = true;
		}
		function closeDrawer() {
			showDrawer.value = false;
		}
		function handleImageUpload(event) {
			const file = event.target.files[0];
			if (!file) return;
			if (file.size > 2 * 1024 * 1024) {
				toast.error("Ukuran gambar maksimal 2MB");
				return;
			}
			form.value.image = file;
			form.value.imagePreview = URL.createObjectURL(file);
		}
		async function saveProduct() {
			formError.value = null;
			saving.value = true;
			try {
				const payload = {
					name: form.value.name,
					sku: form.value.sku || void 0,
					price: parseFloat(form.value.price),
					categoryId: form.value.categoryId || void 0,
					trackStock: form.value.trackStock,
					isActive: form.value.isActive
				};
				if (modalMode.value === "create") {
					await api.post("/api/v1/products", payload);
					toast.success("Produk berhasil ditambahkan!");
				} else {
					await api.put(`/api/v1/products/${form.value.id}`, payload);
					toast.success("Produk berhasil diperbarui!");
				}
				showDrawer.value = false;
				fetchProducts(pagination.value.page);
			} catch (err) {
				formError.value = err.response?.data?.data?.message || err.response?.data?.message || "Gagal menyimpan produk.";
			} finally {
				saving.value = false;
			}
		}
		const deleteModal = ref({
			show: false,
			product: null,
			confirmText: ""
		});
		const deleting = ref(false);
		function doDelete(product) {
			deleteModal.value = {
				show: true,
				product,
				confirmText: ""
			};
		}
		function closeDeleteModal() {
			deleteModal.value.show = false;
			setTimeout(() => {
				deleteModal.value.product = null;
				deleteModal.value.confirmText = "";
			}, 300);
		}
		async function confirmDelete() {
			if (deleteModal.value.confirmText !== deleteModal.value.product?.name) return;
			deleting.value = true;
			try {
				await api.delete(`/api/v1/products/${deleteModal.value.product.id}`);
				toast.success("Produk berhasil dihapus!");
				fetchProducts(pagination.value.page);
				closeDeleteModal();
			} catch (err) {
				toast.error(err.response?.data?.message || "Gagal menghapus produk.");
			} finally {
				deleting.value = false;
			}
		}
		function formatCurrency(value) {
			if (value == null || value === "") return "-";
			return new Intl.NumberFormat("id-ID", {
				style: "currency",
				currency: "IDR",
				minimumFractionDigits: 0
			}).format(value);
		}
		function formatDate(dt) {
			if (!dt) return "-";
			return new Date(dt).toLocaleDateString("id-ID", {
				day: "2-digit",
				month: "short",
				year: "numeric"
			});
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
			_push(ssrRenderComponent(_sfc_main$2, _attrs, {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) {
						_push(`<div class="flex flex-col gap-6 p-4 sm:p-6 pb-20" data-v-11961a06${_scopeId}><div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between" data-v-11961a06${_scopeId}><div data-v-11961a06${_scopeId}><h1 class="text-2xl font-bold tracking-tight" data-v-11961a06${_scopeId}>Produk</h1><p class="text-muted-foreground text-sm mt-1" data-v-11961a06${_scopeId}> Kelola data produk Anda. </p></div><div class="flex flex-col sm:flex-row items-center gap-3 w-full md:w-auto" data-v-11961a06${_scopeId}><div class="w-full sm:w-72" data-v-11961a06${_scopeId}>`);
						_push(ssrRenderComponent(_sfc_main$9, {
							modelValue: searchQuery.value,
							"onUpdate:modelValue": ($event) => searchQuery.value = $event,
							placeholder: "Cari produk..."
						}, null, _parent, _scopeId));
						_push(`</div><div class="flex items-center gap-2 w-full sm:w-auto" data-v-11961a06${_scopeId}><div class="relative flex-1 sm:flex-none" data-v-11961a06${_scopeId}><button class="w-full flex items-center justify-center gap-2 h-9 px-3 rounded-md border border-border bg-background hover:bg-accent text-foreground text-sm font-medium transition-colors" data-v-11961a06${_scopeId}>`);
						_push(ssrRenderComponent(unref(Filter), { class: "h-3.5 w-3.5" }, null, _parent, _scopeId));
						_push(`<span data-v-11961a06${_scopeId}>Filter</span>`);
						if (activeFilterCount.value > 0) _push(`<span class="inline-flex items-center justify-center h-4.5 min-w-[18px] px-1 rounded-full bg-primary text-primary-foreground text-[10px] font-bold leading-none" data-v-11961a06${_scopeId}>${ssrInterpolate(activeFilterCount.value)}</span>`);
						else _push(`<!---->`);
						_push(ssrRenderComponent(unref(ChevronDown), {
							class: ["h-3 w-3 text-muted-foreground", showFilter.value ? "rotate-180" : ""],
							style: { "transition": "transform 0.2s" }
						}, null, _parent, _scopeId));
						_push(`</button>`);
						if (showFilter.value) {
							_push(`<div class="absolute left-0 sm:left-auto sm:right-0 top-full mt-1 z-30 w-64 bg-card border border-border rounded-lg shadow-xl overflow-hidden" data-v-11961a06${_scopeId}><div class="flex items-center justify-between px-3 py-2.5 border-b border-border" data-v-11961a06${_scopeId}><span class="text-xs font-semibold text-foreground uppercase tracking-wide" data-v-11961a06${_scopeId}>Filter</span>`);
							if (activeFilterCount.value > 0) _push(`<button class="text-xs text-red-500 hover:text-red-600 font-medium transition-colors" data-v-11961a06${_scopeId}>Clear all</button>`);
							else _push(`<!---->`);
							_push(`</div><div class="px-3 pt-3 pb-2" data-v-11961a06${_scopeId}><p class="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider mb-2" data-v-11961a06${_scopeId}>Urutkan</p><div class="space-y-1" data-v-11961a06${_scopeId}><!--[-->`);
							ssrRenderList([
								{
									val: "terbaru",
									label: "Terbaru"
								},
								{
									val: "harga-termahal",
									label: "Harga Tertinggi"
								},
								{
									val: "harga-termurah",
									label: "Harga Terendah"
								}
							], (sortOption) => {
								_push(`<label class="flex items-center gap-2.5 px-2 py-1.5 rounded-md hover:bg-muted/50 cursor-pointer transition-colors" data-v-11961a06${_scopeId}><div class="${ssrRenderClass([sortBy.value === sortOption.val ? "bg-primary border-primary" : "border-border bg-background", "relative h-4 w-4 shrink-0 rounded border-2 flex items-center justify-center transition-all"])}" data-v-11961a06${_scopeId}>`);
								if (sortBy.value === sortOption.val) _push(`<svg xmlns="http://www.w3.org/2000/svg" class="h-2.5 w-2.5 text-primary-foreground" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3.5" data-v-11961a06${_scopeId}><polyline points="20 6 9 17 4 12" data-v-11961a06${_scopeId}></polyline></svg>`);
								else _push(`<!---->`);
								_push(`</div><span class="text-sm text-foreground select-none" data-v-11961a06${_scopeId}>${ssrInterpolate(sortOption.label)}</span></label>`);
							});
							_push(`<!--]--></div></div><div class="mx-3 border-t border-border" data-v-11961a06${_scopeId}></div><div class="px-3 pt-2 pb-2" data-v-11961a06${_scopeId}><p class="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider mb-2" data-v-11961a06${_scopeId}>Status</p><div class="space-y-1" data-v-11961a06${_scopeId}><!--[-->`);
							ssrRenderList([
								{
									val: "all",
									label: "Semua Status"
								},
								{
									val: "aktif",
									label: "Aktif"
								},
								{
									val: "nonaktif",
									label: "Nonaktif"
								}
							], (statusOption) => {
								_push(`<label class="flex items-center gap-2.5 px-2 py-1.5 rounded-md hover:bg-muted/50 cursor-pointer transition-colors" data-v-11961a06${_scopeId}><div class="${ssrRenderClass([filterStatus.value === statusOption.val ? "bg-primary border-primary" : "border-border bg-background", "relative h-4 w-4 shrink-0 rounded border-2 flex items-center justify-center transition-all"])}" data-v-11961a06${_scopeId}>`);
								if (filterStatus.value === statusOption.val) _push(`<svg xmlns="http://www.w3.org/2000/svg" class="h-2.5 w-2.5 text-primary-foreground" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3.5" data-v-11961a06${_scopeId}><polyline points="20 6 9 17 4 12" data-v-11961a06${_scopeId}></polyline></svg>`);
								else _push(`<!---->`);
								_push(`</div><span class="text-sm text-foreground select-none" data-v-11961a06${_scopeId}>${ssrInterpolate(statusOption.label)}</span></label>`);
							});
							_push(`<!--]--></div></div><div class="mx-3 border-t border-border" data-v-11961a06${_scopeId}></div><div class="px-3 pt-2 pb-3" data-v-11961a06${_scopeId}><p class="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider mb-2" data-v-11961a06${_scopeId}>Stok</p><div class="space-y-1" data-v-11961a06${_scopeId}><!--[-->`);
							ssrRenderList([
								{
									val: "all",
									label: "Semua Stok"
								},
								{
									val: "dilacak",
									label: "Dilacak"
								},
								{
									val: "bebas",
									label: "Bebas"
								}
							], (stockOption) => {
								_push(`<label class="flex items-center gap-2.5 px-2 py-1.5 rounded-md hover:bg-muted/50 cursor-pointer transition-colors" data-v-11961a06${_scopeId}><div class="${ssrRenderClass([filterStock.value === stockOption.val ? "bg-primary border-primary" : "border-border bg-background", "relative h-4 w-4 shrink-0 rounded border-2 flex items-center justify-center transition-all"])}" data-v-11961a06${_scopeId}>`);
								if (filterStock.value === stockOption.val) _push(`<svg xmlns="http://www.w3.org/2000/svg" class="h-2.5 w-2.5 text-primary-foreground" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3.5" data-v-11961a06${_scopeId}><polyline points="20 6 9 17 4 12" data-v-11961a06${_scopeId}></polyline></svg>`);
								else _push(`<!---->`);
								_push(`</div><span class="text-sm text-foreground select-none" data-v-11961a06${_scopeId}>${ssrInterpolate(stockOption.label)}</span></label>`);
							});
							_push(`<!--]--></div></div></div>`);
						} else _push(`<!---->`);
						_push(`</div>`);
						if (unref(can)("produk.store")) _push(ssrRenderComponent(_sfc_main$3, {
							onClick: openCreate,
							size: "sm",
							class: "flex-1 sm:flex-none flex items-center justify-center gap-2 bg-primary hover:bg-primary/90 text-primary-foreground"
						}, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) {
									_push(ssrRenderComponent(unref(Plus), { class: "h-4 w-4" }, null, _parent, _scopeId));
									_push(`<span data-v-11961a06${_scopeId}>Tambah Produk</span>`);
								} else return [createVNode(unref(Plus), { class: "h-4 w-4" }), createVNode("span", null, "Tambah Produk")];
							}),
							_: 1
						}, _parent, _scopeId));
						else _push(`<!---->`);
						_push(`</div></div></div>`);
						if (error.value) _push(ssrRenderComponent(_sfc_main$7, {
							variant: "destructive",
							class: "mb-4"
						}, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) _push(`${ssrInterpolate(error.value)}`);
								else return [createTextVNode(toDisplayString(error.value), 1)];
							}),
							_: 1
						}, _parent, _scopeId));
						else _push(`<!---->`);
						_push(ssrRenderComponent(_sfc_main$4, { class: "border-zinc-200 dark:border-zinc-800 shadow-sm overflow-hidden" }, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) _push(ssrRenderComponent(_sfc_main$5, { class: "p-0" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) {
											if (loading.value) {
												_push(`<div class="flex flex-col items-center justify-center py-24 gap-3" data-v-11961a06${_scopeId}>`);
												_push(ssrRenderComponent(unref(Loader2), { class: "h-7 w-7 animate-spin text-primary/50" }, null, _parent, _scopeId));
												_push(`<p class="text-xs text-muted-foreground" data-v-11961a06${_scopeId}>Memuat data...</p></div>`);
											} else if (filteredProducts.value.length === 0) {
												_push(`<div class="flex flex-col items-center justify-center py-24 text-muted-foreground" data-v-11961a06${_scopeId}><div class="w-14 h-14 rounded-full bg-muted flex items-center justify-center mb-4" data-v-11961a06${_scopeId}>`);
												_push(ssrRenderComponent(unref(Package), { class: "h-7 w-7 opacity-40" }, null, _parent, _scopeId));
												_push(`</div><p class="text-sm font-medium" data-v-11961a06${_scopeId}>Belum ada produk</p><p class="text-xs text-muted-foreground/70 mt-1" data-v-11961a06${_scopeId}>Mulai dengan menambahkan produk pertama Anda.</p>`);
												if (unref(can)("produk.store") && !searchQuery.value) _push(ssrRenderComponent(_sfc_main$3, {
													size: "sm",
													class: "mt-4",
													onClick: openCreate
												}, {
													default: withCtx((_, _push, _parent, _scopeId) => {
														if (_push) {
															_push(ssrRenderComponent(unref(Plus), { class: "h-3.5 w-3.5 mr-1.5" }, null, _parent, _scopeId));
															_push(` Tambah Produk `);
														} else return [createVNode(unref(Plus), { class: "h-3.5 w-3.5 mr-1.5" }), createTextVNode(" Tambah Produk ")];
													}),
													_: 1
												}, _parent, _scopeId));
												else _push(`<!---->`);
												_push(`</div>`);
											} else {
												_push(`<div data-v-11961a06${_scopeId}><div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60" data-v-11961a06${_scopeId}><!--[-->`);
												ssrRenderList(filteredProducts.value, (product) => {
													_push(`<div class="p-4 flex flex-col gap-3 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors" data-v-11961a06${_scopeId}><div class="flex items-start justify-between gap-3" data-v-11961a06${_scopeId}><div class="flex items-center gap-3" data-v-11961a06${_scopeId}>`);
													if (product.imageUrl) _push(`<img${ssrRenderAttr("src", product.imageUrl)} class="w-12 h-12 rounded-lg object-cover border border-zinc-200 dark:border-zinc-800" data-v-11961a06${_scopeId}>`);
													else _push(`<div class="w-12 h-12 rounded-lg flex items-center justify-center text-sm font-bold shrink-0 select-none border border-zinc-200 dark:border-zinc-800/50" style="${ssrRenderStyle(productAvatarStyle(product.name))}" data-v-11961a06${_scopeId}>${ssrInterpolate(product.name?.charAt(0).toUpperCase())}</div>`);
													_push(`<div data-v-11961a06${_scopeId}><h4 class="font-medium text-sm text-zinc-900 dark:text-zinc-100 line-clamp-1" data-v-11961a06${_scopeId}>${ssrInterpolate(product.name)}</h4>`);
													if (product.sku) _push(`<span class="font-mono text-[10px] bg-zinc-100 dark:bg-zinc-800 text-zinc-500 px-1.5 py-0.5 rounded mt-1 inline-block" data-v-11961a06${_scopeId}>${ssrInterpolate(product.sku)}</span>`);
													else _push(`<!---->`);
													_push(`</div></div><div class="flex items-center gap-1 shrink-0" data-v-11961a06${_scopeId}>`);
													if (unref(can)("produk.update")) _push(ssrRenderComponent(_sfc_main$3, {
														variant: "ghost",
														size: "icon",
														class: "h-8 w-8 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 bg-zinc-50 dark:bg-zinc-800/50",
														onClick: ($event) => openEdit(product)
													}, {
														default: withCtx((_, _push, _parent, _scopeId) => {
															if (_push) _push(ssrRenderComponent(unref(Pencil), { class: "h-3.5 w-3.5" }, null, _parent, _scopeId));
															else return [createVNode(unref(Pencil), { class: "h-3.5 w-3.5" })];
														}),
														_: 2
													}, _parent, _scopeId));
													else _push(`<!---->`);
													if (unref(can)("produk.delete")) _push(ssrRenderComponent(_sfc_main$3, {
														variant: "ghost",
														size: "icon",
														class: "h-8 w-8 text-zinc-400 hover:text-destructive bg-zinc-50 dark:bg-zinc-800/50",
														onClick: ($event) => doDelete(product)
													}, {
														default: withCtx((_, _push, _parent, _scopeId) => {
															if (_push) _push(ssrRenderComponent(unref(Trash2), { class: "h-3.5 w-3.5" }, null, _parent, _scopeId));
															else return [createVNode(unref(Trash2), { class: "h-3.5 w-3.5" })];
														}),
														_: 2
													}, _parent, _scopeId));
													else _push(`<!---->`);
													_push(`</div></div><div class="flex items-center justify-between mt-1" data-v-11961a06${_scopeId}><span class="text-sm font-semibold text-zinc-800 dark:text-zinc-200" data-v-11961a06${_scopeId}>${ssrInterpolate(formatCurrency(product.price))}</span><div class="flex items-center gap-2" data-v-11961a06${_scopeId}>`);
													if (product.categoryName) _push(`<span class="text-[10px] font-medium px-2 py-0.5 rounded-full bg-blue-50 text-blue-700 dark:bg-blue-900/20 dark:text-blue-300 border border-blue-100 dark:border-blue-800/40" data-v-11961a06${_scopeId}>${ssrInterpolate(product.categoryName)}</span>`);
													else _push(`<!---->`);
													_push(`<span class="${ssrRenderClass([product.isActive ? "bg-primary/10 text-primary border border-primary/20" : "bg-zinc-100 dark:bg-zinc-800 text-zinc-500 border border-zinc-200 dark:border-zinc-700", "inline-flex items-center gap-1 text-[10px] font-medium px-2 py-0.5 rounded-full"])}" data-v-11961a06${_scopeId}><span class="${ssrRenderClass([product.isActive ? "bg-primary" : "bg-zinc-400", "w-1 h-1 rounded-full"])}" data-v-11961a06${_scopeId}></span> ${ssrInterpolate(product.isActive ? "Aktif" : "Nonaktif")}</span></div></div></div>`);
												});
												_push(`<!--]--></div><div class="hidden md:block overflow-x-auto" data-v-11961a06${_scopeId}>`);
												_push(ssrRenderComponent(_sfc_main$11, null, {
													default: withCtx((_, _push, _parent, _scopeId) => {
														if (_push) {
															_push(ssrRenderComponent(_sfc_main$12, null, {
																default: withCtx((_, _push, _parent, _scopeId) => {
																	if (_push) _push(ssrRenderComponent(_sfc_main$13, { class: "hover:bg-transparent border-b border-zinc-100 dark:border-zinc-800" }, {
																		default: withCtx((_, _push, _parent, _scopeId) => {
																			if (_push) {
																				_push(ssrRenderComponent(_sfc_main$14, { class: "pl-5 py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500 w-[260px]" }, {
																					default: withCtx((_, _push, _parent, _scopeId) => {
																						if (_push) _push(`Produk`);
																						else return [createTextVNode("Produk")];
																					}),
																					_: 1
																				}, _parent, _scopeId));
																				_push(ssrRenderComponent(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
																					default: withCtx((_, _push, _parent, _scopeId) => {
																						if (_push) _push(`SKU`);
																						else return [createTextVNode("SKU")];
																					}),
																					_: 1
																				}, _parent, _scopeId));
																				_push(ssrRenderComponent(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
																					default: withCtx((_, _push, _parent, _scopeId) => {
																						if (_push) _push(`Harga`);
																						else return [createTextVNode("Harga")];
																					}),
																					_: 1
																				}, _parent, _scopeId));
																				_push(ssrRenderComponent(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
																					default: withCtx((_, _push, _parent, _scopeId) => {
																						if (_push) _push(`Kategori`);
																						else return [createTextVNode("Kategori")];
																					}),
																					_: 1
																				}, _parent, _scopeId));
																				_push(ssrRenderComponent(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500 text-center" }, {
																					default: withCtx((_, _push, _parent, _scopeId) => {
																						if (_push) _push(`Stok`);
																						else return [createTextVNode("Stok")];
																					}),
																					_: 1
																				}, _parent, _scopeId));
																				_push(ssrRenderComponent(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500 text-center" }, {
																					default: withCtx((_, _push, _parent, _scopeId) => {
																						if (_push) _push(`Status`);
																						else return [createTextVNode("Status")];
																					}),
																					_: 1
																				}, _parent, _scopeId));
																				_push(ssrRenderComponent(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
																					default: withCtx((_, _push, _parent, _scopeId) => {
																						if (_push) _push(`Dibuat`);
																						else return [createTextVNode("Dibuat")];
																					}),
																					_: 1
																				}, _parent, _scopeId));
																				_push(ssrRenderComponent(_sfc_main$14, { class: "pr-5 py-3 text-right" }, null, _parent, _scopeId));
																			} else return [
																				createVNode(_sfc_main$14, { class: "pl-5 py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500 w-[260px]" }, {
																					default: withCtx(() => [createTextVNode("Produk")]),
																					_: 1
																				}),
																				createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
																					default: withCtx(() => [createTextVNode("SKU")]),
																					_: 1
																				}),
																				createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
																					default: withCtx(() => [createTextVNode("Harga")]),
																					_: 1
																				}),
																				createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
																					default: withCtx(() => [createTextVNode("Kategori")]),
																					_: 1
																				}),
																				createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500 text-center" }, {
																					default: withCtx(() => [createTextVNode("Stok")]),
																					_: 1
																				}),
																				createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500 text-center" }, {
																					default: withCtx(() => [createTextVNode("Status")]),
																					_: 1
																				}),
																				createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
																					default: withCtx(() => [createTextVNode("Dibuat")]),
																					_: 1
																				}),
																				createVNode(_sfc_main$14, { class: "pr-5 py-3 text-right" })
																			];
																		}),
																		_: 1
																	}, _parent, _scopeId));
																	else return [createVNode(_sfc_main$13, { class: "hover:bg-transparent border-b border-zinc-100 dark:border-zinc-800" }, {
																		default: withCtx(() => [
																			createVNode(_sfc_main$14, { class: "pl-5 py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500 w-[260px]" }, {
																				default: withCtx(() => [createTextVNode("Produk")]),
																				_: 1
																			}),
																			createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
																				default: withCtx(() => [createTextVNode("SKU")]),
																				_: 1
																			}),
																			createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
																				default: withCtx(() => [createTextVNode("Harga")]),
																				_: 1
																			}),
																			createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
																				default: withCtx(() => [createTextVNode("Kategori")]),
																				_: 1
																			}),
																			createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500 text-center" }, {
																				default: withCtx(() => [createTextVNode("Stok")]),
																				_: 1
																			}),
																			createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500 text-center" }, {
																				default: withCtx(() => [createTextVNode("Status")]),
																				_: 1
																			}),
																			createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
																				default: withCtx(() => [createTextVNode("Dibuat")]),
																				_: 1
																			}),
																			createVNode(_sfc_main$14, { class: "pr-5 py-3 text-right" })
																		]),
																		_: 1
																	})];
																}),
																_: 1
															}, _parent, _scopeId));
															_push(ssrRenderComponent(_sfc_main$15, null, {
																default: withCtx((_, _push, _parent, _scopeId) => {
																	if (_push) {
																		_push(`<!--[-->`);
																		ssrRenderList(filteredProducts.value, (product) => {
																			_push(ssrRenderComponent(_sfc_main$13, {
																				key: product.id,
																				class: "group border-b border-zinc-100 dark:border-zinc-800/60 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors"
																			}, {
																				default: withCtx((_, _push, _parent, _scopeId) => {
																					if (_push) {
																						_push(ssrRenderComponent(_sfc_main$16, { class: "pl-5 py-3" }, {
																							default: withCtx((_, _push, _parent, _scopeId) => {
																								if (_push) {
																									_push(`<div class="flex items-center gap-3" data-v-11961a06${_scopeId}>`);
																									if (product.imageUrl) _push(`<img${ssrRenderAttr("src", product.imageUrl)} class="w-8 h-8 rounded-lg object-cover border border-zinc-200 dark:border-zinc-700" data-v-11961a06${_scopeId}>`);
																									else _push(`<div class="w-8 h-8 rounded-lg flex items-center justify-center text-xs font-bold shrink-0 select-none border border-zinc-200 dark:border-zinc-700/50" style="${ssrRenderStyle(productAvatarStyle(product.name))}" data-v-11961a06${_scopeId}>${ssrInterpolate(product.name?.charAt(0).toUpperCase())}</div>`);
																									_push(`<span class="font-medium text-sm text-zinc-900 dark:text-zinc-100 truncate max-w-[170px]" data-v-11961a06${_scopeId}>${ssrInterpolate(product.name)}</span></div>`);
																								} else return [createVNode("div", { class: "flex items-center gap-3" }, [product.imageUrl ? (openBlock(), createBlock("img", {
																									key: 0,
																									src: product.imageUrl,
																									class: "w-8 h-8 rounded-lg object-cover border border-zinc-200 dark:border-zinc-700"
																								}, null, 8, ["src"])) : (openBlock(), createBlock("div", {
																									key: 1,
																									class: "w-8 h-8 rounded-lg flex items-center justify-center text-xs font-bold shrink-0 select-none border border-zinc-200 dark:border-zinc-700/50",
																									style: productAvatarStyle(product.name)
																								}, toDisplayString(product.name?.charAt(0).toUpperCase()), 5)), createVNode("span", { class: "font-medium text-sm text-zinc-900 dark:text-zinc-100 truncate max-w-[170px]" }, toDisplayString(product.name), 1)])];
																							}),
																							_: 2
																						}, _parent, _scopeId));
																						_push(ssrRenderComponent(_sfc_main$16, { class: "py-3" }, {
																							default: withCtx((_, _push, _parent, _scopeId) => {
																								if (_push) if (product.sku) _push(`<span class="font-mono text-xs bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-400 px-2 py-1 rounded" data-v-11961a06${_scopeId}>${ssrInterpolate(product.sku)}</span>`);
																								else _push(`<span class="text-zinc-300 dark:text-zinc-600 text-sm" data-v-11961a06${_scopeId}>—</span>`);
																								else return [product.sku ? (openBlock(), createBlock("span", {
																									key: 0,
																									class: "font-mono text-xs bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-400 px-2 py-1 rounded"
																								}, toDisplayString(product.sku), 1)) : (openBlock(), createBlock("span", {
																									key: 1,
																									class: "text-zinc-300 dark:text-zinc-600 text-sm"
																								}, "—"))];
																							}),
																							_: 2
																						}, _parent, _scopeId));
																						_push(ssrRenderComponent(_sfc_main$16, { class: "py-3" }, {
																							default: withCtx((_, _push, _parent, _scopeId) => {
																								if (_push) _push(`<span class="text-sm font-semibold text-zinc-800 dark:text-zinc-200" data-v-11961a06${_scopeId}>${ssrInterpolate(formatCurrency(product.price))}</span>`);
																								else return [createVNode("span", { class: "text-sm font-semibold text-zinc-800 dark:text-zinc-200" }, toDisplayString(formatCurrency(product.price)), 1)];
																							}),
																							_: 2
																						}, _parent, _scopeId));
																						_push(ssrRenderComponent(_sfc_main$16, { class: "py-3" }, {
																							default: withCtx((_, _push, _parent, _scopeId) => {
																								if (_push) if (product.categoryName) _push(`<span class="inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full bg-blue-50 dark:bg-blue-900/20 text-blue-700 dark:text-blue-300 border border-blue-100 dark:border-blue-800/40" data-v-11961a06${_scopeId}>${ssrInterpolate(product.categoryName)}</span>`);
																								else _push(`<span class="text-zinc-300 dark:text-zinc-600 text-sm" data-v-11961a06${_scopeId}>—</span>`);
																								else return [product.categoryName ? (openBlock(), createBlock("span", {
																									key: 0,
																									class: "inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full bg-blue-50 dark:bg-blue-900/20 text-blue-700 dark:text-blue-300 border border-blue-100 dark:border-blue-800/40"
																								}, toDisplayString(product.categoryName), 1)) : (openBlock(), createBlock("span", {
																									key: 1,
																									class: "text-zinc-300 dark:text-zinc-600 text-sm"
																								}, "—"))];
																							}),
																							_: 2
																						}, _parent, _scopeId));
																						_push(ssrRenderComponent(_sfc_main$16, { class: "py-3 text-center" }, {
																							default: withCtx((_, _push, _parent, _scopeId) => {
																								if (_push) _push(`<span class="${ssrRenderClass([product.trackStock ? "bg-emerald-50 dark:bg-emerald-900/20 text-emerald-700 dark:text-emerald-400 border border-emerald-100 dark:border-emerald-800/40" : "bg-zinc-100 dark:bg-zinc-800 text-zinc-500 border border-zinc-200 dark:border-zinc-700", "inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full"])}" data-v-11961a06${_scopeId}><span class="${ssrRenderClass([product.trackStock ? "bg-emerald-500" : "bg-zinc-400", "w-1.5 h-1.5 rounded-full"])}" data-v-11961a06${_scopeId}></span> ${ssrInterpolate(product.trackStock ? "Dilacak" : "Bebas")}</span>`);
																								else return [createVNode("span", { class: ["inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full", product.trackStock ? "bg-emerald-50 dark:bg-emerald-900/20 text-emerald-700 dark:text-emerald-400 border border-emerald-100 dark:border-emerald-800/40" : "bg-zinc-100 dark:bg-zinc-800 text-zinc-500 border border-zinc-200 dark:border-zinc-700"] }, [createVNode("span", { class: ["w-1.5 h-1.5 rounded-full", product.trackStock ? "bg-emerald-500" : "bg-zinc-400"] }, null, 2), createTextVNode(" " + toDisplayString(product.trackStock ? "Dilacak" : "Bebas"), 1)], 2)];
																							}),
																							_: 2
																						}, _parent, _scopeId));
																						_push(ssrRenderComponent(_sfc_main$16, { class: "py-3 text-center" }, {
																							default: withCtx((_, _push, _parent, _scopeId) => {
																								if (_push) _push(`<span class="${ssrRenderClass([product.isActive ? "bg-primary/10 text-primary border border-primary/20" : "bg-zinc-100 dark:bg-zinc-800 text-zinc-500 border border-zinc-200 dark:border-zinc-700", "inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full"])}" data-v-11961a06${_scopeId}><span class="${ssrRenderClass([product.isActive ? "bg-primary" : "bg-zinc-400", "w-1.5 h-1.5 rounded-full"])}" data-v-11961a06${_scopeId}></span> ${ssrInterpolate(product.isActive ? "Aktif" : "Nonaktif")}</span>`);
																								else return [createVNode("span", { class: ["inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full", product.isActive ? "bg-primary/10 text-primary border border-primary/20" : "bg-zinc-100 dark:bg-zinc-800 text-zinc-500 border border-zinc-200 dark:border-zinc-700"] }, [createVNode("span", { class: ["w-1.5 h-1.5 rounded-full", product.isActive ? "bg-primary" : "bg-zinc-400"] }, null, 2), createTextVNode(" " + toDisplayString(product.isActive ? "Aktif" : "Nonaktif"), 1)], 2)];
																							}),
																							_: 2
																						}, _parent, _scopeId));
																						_push(ssrRenderComponent(_sfc_main$16, { class: "py-3" }, {
																							default: withCtx((_, _push, _parent, _scopeId) => {
																								if (_push) _push(`<span class="text-xs text-zinc-400 dark:text-zinc-500" data-v-11961a06${_scopeId}>${ssrInterpolate(formatDate(product.createdAt))}</span>`);
																								else return [createVNode("span", { class: "text-xs text-zinc-400 dark:text-zinc-500" }, toDisplayString(formatDate(product.createdAt)), 1)];
																							}),
																							_: 2
																						}, _parent, _scopeId));
																						_push(ssrRenderComponent(_sfc_main$16, { class: "pr-4 py-3 text-right" }, {
																							default: withCtx((_, _push, _parent, _scopeId) => {
																								if (_push) {
																									_push(`<div class="flex justify-end gap-1 transition-opacity" data-v-11961a06${_scopeId}>`);
																									if (unref(can)("produk.update")) _push(ssrRenderComponent(_sfc_main$3, {
																										variant: "ghost",
																										size: "icon",
																										class: "h-7 w-7 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 hover:bg-zinc-100 dark:hover:bg-zinc-800 rounded-md",
																										title: "Edit",
																										onClick: ($event) => openEdit(product)
																									}, {
																										default: withCtx((_, _push, _parent, _scopeId) => {
																											if (_push) _push(ssrRenderComponent(unref(Pencil), { class: "h-3.5 w-3.5" }, null, _parent, _scopeId));
																											else return [createVNode(unref(Pencil), { class: "h-3.5 w-3.5" })];
																										}),
																										_: 2
																									}, _parent, _scopeId));
																									else _push(`<!---->`);
																									if (unref(can)("produk.delete")) _push(ssrRenderComponent(_sfc_main$3, {
																										variant: "ghost",
																										size: "icon",
																										class: "h-7 w-7 text-zinc-400 hover:text-destructive hover:bg-destructive/10 rounded-md",
																										title: "Hapus",
																										onClick: ($event) => doDelete(product)
																									}, {
																										default: withCtx((_, _push, _parent, _scopeId) => {
																											if (_push) _push(ssrRenderComponent(unref(Trash2), { class: "h-3.5 w-3.5" }, null, _parent, _scopeId));
																											else return [createVNode(unref(Trash2), { class: "h-3.5 w-3.5" })];
																										}),
																										_: 2
																									}, _parent, _scopeId));
																									else _push(`<!---->`);
																									_push(`</div>`);
																								} else return [createVNode("div", { class: "flex justify-end gap-1 transition-opacity" }, [unref(can)("produk.update") ? (openBlock(), createBlock(_sfc_main$3, {
																									key: 0,
																									variant: "ghost",
																									size: "icon",
																									class: "h-7 w-7 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 hover:bg-zinc-100 dark:hover:bg-zinc-800 rounded-md",
																									title: "Edit",
																									onClick: ($event) => openEdit(product)
																								}, {
																									default: withCtx(() => [createVNode(unref(Pencil), { class: "h-3.5 w-3.5" })]),
																									_: 1
																								}, 8, ["onClick"])) : createCommentVNode("", true), unref(can)("produk.delete") ? (openBlock(), createBlock(_sfc_main$3, {
																									key: 1,
																									variant: "ghost",
																									size: "icon",
																									class: "h-7 w-7 text-zinc-400 hover:text-destructive hover:bg-destructive/10 rounded-md",
																									title: "Hapus",
																									onClick: ($event) => doDelete(product)
																								}, {
																									default: withCtx(() => [createVNode(unref(Trash2), { class: "h-3.5 w-3.5" })]),
																									_: 1
																								}, 8, ["onClick"])) : createCommentVNode("", true)])];
																							}),
																							_: 2
																						}, _parent, _scopeId));
																					} else return [
																						createVNode(_sfc_main$16, { class: "pl-5 py-3" }, {
																							default: withCtx(() => [createVNode("div", { class: "flex items-center gap-3" }, [product.imageUrl ? (openBlock(), createBlock("img", {
																								key: 0,
																								src: product.imageUrl,
																								class: "w-8 h-8 rounded-lg object-cover border border-zinc-200 dark:border-zinc-700"
																							}, null, 8, ["src"])) : (openBlock(), createBlock("div", {
																								key: 1,
																								class: "w-8 h-8 rounded-lg flex items-center justify-center text-xs font-bold shrink-0 select-none border border-zinc-200 dark:border-zinc-700/50",
																								style: productAvatarStyle(product.name)
																							}, toDisplayString(product.name?.charAt(0).toUpperCase()), 5)), createVNode("span", { class: "font-medium text-sm text-zinc-900 dark:text-zinc-100 truncate max-w-[170px]" }, toDisplayString(product.name), 1)])]),
																							_: 2
																						}, 1024),
																						createVNode(_sfc_main$16, { class: "py-3" }, {
																							default: withCtx(() => [product.sku ? (openBlock(), createBlock("span", {
																								key: 0,
																								class: "font-mono text-xs bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-400 px-2 py-1 rounded"
																							}, toDisplayString(product.sku), 1)) : (openBlock(), createBlock("span", {
																								key: 1,
																								class: "text-zinc-300 dark:text-zinc-600 text-sm"
																							}, "—"))]),
																							_: 2
																						}, 1024),
																						createVNode(_sfc_main$16, { class: "py-3" }, {
																							default: withCtx(() => [createVNode("span", { class: "text-sm font-semibold text-zinc-800 dark:text-zinc-200" }, toDisplayString(formatCurrency(product.price)), 1)]),
																							_: 2
																						}, 1024),
																						createVNode(_sfc_main$16, { class: "py-3" }, {
																							default: withCtx(() => [product.categoryName ? (openBlock(), createBlock("span", {
																								key: 0,
																								class: "inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full bg-blue-50 dark:bg-blue-900/20 text-blue-700 dark:text-blue-300 border border-blue-100 dark:border-blue-800/40"
																							}, toDisplayString(product.categoryName), 1)) : (openBlock(), createBlock("span", {
																								key: 1,
																								class: "text-zinc-300 dark:text-zinc-600 text-sm"
																							}, "—"))]),
																							_: 2
																						}, 1024),
																						createVNode(_sfc_main$16, { class: "py-3 text-center" }, {
																							default: withCtx(() => [createVNode("span", { class: ["inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full", product.trackStock ? "bg-emerald-50 dark:bg-emerald-900/20 text-emerald-700 dark:text-emerald-400 border border-emerald-100 dark:border-emerald-800/40" : "bg-zinc-100 dark:bg-zinc-800 text-zinc-500 border border-zinc-200 dark:border-zinc-700"] }, [createVNode("span", { class: ["w-1.5 h-1.5 rounded-full", product.trackStock ? "bg-emerald-500" : "bg-zinc-400"] }, null, 2), createTextVNode(" " + toDisplayString(product.trackStock ? "Dilacak" : "Bebas"), 1)], 2)]),
																							_: 2
																						}, 1024),
																						createVNode(_sfc_main$16, { class: "py-3 text-center" }, {
																							default: withCtx(() => [createVNode("span", { class: ["inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full", product.isActive ? "bg-primary/10 text-primary border border-primary/20" : "bg-zinc-100 dark:bg-zinc-800 text-zinc-500 border border-zinc-200 dark:border-zinc-700"] }, [createVNode("span", { class: ["w-1.5 h-1.5 rounded-full", product.isActive ? "bg-primary" : "bg-zinc-400"] }, null, 2), createTextVNode(" " + toDisplayString(product.isActive ? "Aktif" : "Nonaktif"), 1)], 2)]),
																							_: 2
																						}, 1024),
																						createVNode(_sfc_main$16, { class: "py-3" }, {
																							default: withCtx(() => [createVNode("span", { class: "text-xs text-zinc-400 dark:text-zinc-500" }, toDisplayString(formatDate(product.createdAt)), 1)]),
																							_: 2
																						}, 1024),
																						createVNode(_sfc_main$16, { class: "pr-4 py-3 text-right" }, {
																							default: withCtx(() => [createVNode("div", { class: "flex justify-end gap-1 transition-opacity" }, [unref(can)("produk.update") ? (openBlock(), createBlock(_sfc_main$3, {
																								key: 0,
																								variant: "ghost",
																								size: "icon",
																								class: "h-7 w-7 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 hover:bg-zinc-100 dark:hover:bg-zinc-800 rounded-md",
																								title: "Edit",
																								onClick: ($event) => openEdit(product)
																							}, {
																								default: withCtx(() => [createVNode(unref(Pencil), { class: "h-3.5 w-3.5" })]),
																								_: 1
																							}, 8, ["onClick"])) : createCommentVNode("", true), unref(can)("produk.delete") ? (openBlock(), createBlock(_sfc_main$3, {
																								key: 1,
																								variant: "ghost",
																								size: "icon",
																								class: "h-7 w-7 text-zinc-400 hover:text-destructive hover:bg-destructive/10 rounded-md",
																								title: "Hapus",
																								onClick: ($event) => doDelete(product)
																							}, {
																								default: withCtx(() => [createVNode(unref(Trash2), { class: "h-3.5 w-3.5" })]),
																								_: 1
																							}, 8, ["onClick"])) : createCommentVNode("", true)])]),
																							_: 2
																						}, 1024)
																					];
																				}),
																				_: 2
																			}, _parent, _scopeId));
																		});
																		_push(`<!--]-->`);
																	} else return [(openBlock(true), createBlock(Fragment, null, renderList(filteredProducts.value, (product) => {
																		return openBlock(), createBlock(_sfc_main$13, {
																			key: product.id,
																			class: "group border-b border-zinc-100 dark:border-zinc-800/60 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors"
																		}, {
																			default: withCtx(() => [
																				createVNode(_sfc_main$16, { class: "pl-5 py-3" }, {
																					default: withCtx(() => [createVNode("div", { class: "flex items-center gap-3" }, [product.imageUrl ? (openBlock(), createBlock("img", {
																						key: 0,
																						src: product.imageUrl,
																						class: "w-8 h-8 rounded-lg object-cover border border-zinc-200 dark:border-zinc-700"
																					}, null, 8, ["src"])) : (openBlock(), createBlock("div", {
																						key: 1,
																						class: "w-8 h-8 rounded-lg flex items-center justify-center text-xs font-bold shrink-0 select-none border border-zinc-200 dark:border-zinc-700/50",
																						style: productAvatarStyle(product.name)
																					}, toDisplayString(product.name?.charAt(0).toUpperCase()), 5)), createVNode("span", { class: "font-medium text-sm text-zinc-900 dark:text-zinc-100 truncate max-w-[170px]" }, toDisplayString(product.name), 1)])]),
																					_: 2
																				}, 1024),
																				createVNode(_sfc_main$16, { class: "py-3" }, {
																					default: withCtx(() => [product.sku ? (openBlock(), createBlock("span", {
																						key: 0,
																						class: "font-mono text-xs bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-400 px-2 py-1 rounded"
																					}, toDisplayString(product.sku), 1)) : (openBlock(), createBlock("span", {
																						key: 1,
																						class: "text-zinc-300 dark:text-zinc-600 text-sm"
																					}, "—"))]),
																					_: 2
																				}, 1024),
																				createVNode(_sfc_main$16, { class: "py-3" }, {
																					default: withCtx(() => [createVNode("span", { class: "text-sm font-semibold text-zinc-800 dark:text-zinc-200" }, toDisplayString(formatCurrency(product.price)), 1)]),
																					_: 2
																				}, 1024),
																				createVNode(_sfc_main$16, { class: "py-3" }, {
																					default: withCtx(() => [product.categoryName ? (openBlock(), createBlock("span", {
																						key: 0,
																						class: "inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full bg-blue-50 dark:bg-blue-900/20 text-blue-700 dark:text-blue-300 border border-blue-100 dark:border-blue-800/40"
																					}, toDisplayString(product.categoryName), 1)) : (openBlock(), createBlock("span", {
																						key: 1,
																						class: "text-zinc-300 dark:text-zinc-600 text-sm"
																					}, "—"))]),
																					_: 2
																				}, 1024),
																				createVNode(_sfc_main$16, { class: "py-3 text-center" }, {
																					default: withCtx(() => [createVNode("span", { class: ["inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full", product.trackStock ? "bg-emerald-50 dark:bg-emerald-900/20 text-emerald-700 dark:text-emerald-400 border border-emerald-100 dark:border-emerald-800/40" : "bg-zinc-100 dark:bg-zinc-800 text-zinc-500 border border-zinc-200 dark:border-zinc-700"] }, [createVNode("span", { class: ["w-1.5 h-1.5 rounded-full", product.trackStock ? "bg-emerald-500" : "bg-zinc-400"] }, null, 2), createTextVNode(" " + toDisplayString(product.trackStock ? "Dilacak" : "Bebas"), 1)], 2)]),
																					_: 2
																				}, 1024),
																				createVNode(_sfc_main$16, { class: "py-3 text-center" }, {
																					default: withCtx(() => [createVNode("span", { class: ["inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full", product.isActive ? "bg-primary/10 text-primary border border-primary/20" : "bg-zinc-100 dark:bg-zinc-800 text-zinc-500 border border-zinc-200 dark:border-zinc-700"] }, [createVNode("span", { class: ["w-1.5 h-1.5 rounded-full", product.isActive ? "bg-primary" : "bg-zinc-400"] }, null, 2), createTextVNode(" " + toDisplayString(product.isActive ? "Aktif" : "Nonaktif"), 1)], 2)]),
																					_: 2
																				}, 1024),
																				createVNode(_sfc_main$16, { class: "py-3" }, {
																					default: withCtx(() => [createVNode("span", { class: "text-xs text-zinc-400 dark:text-zinc-500" }, toDisplayString(formatDate(product.createdAt)), 1)]),
																					_: 2
																				}, 1024),
																				createVNode(_sfc_main$16, { class: "pr-4 py-3 text-right" }, {
																					default: withCtx(() => [createVNode("div", { class: "flex justify-end gap-1 transition-opacity" }, [unref(can)("produk.update") ? (openBlock(), createBlock(_sfc_main$3, {
																						key: 0,
																						variant: "ghost",
																						size: "icon",
																						class: "h-7 w-7 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 hover:bg-zinc-100 dark:hover:bg-zinc-800 rounded-md",
																						title: "Edit",
																						onClick: ($event) => openEdit(product)
																					}, {
																						default: withCtx(() => [createVNode(unref(Pencil), { class: "h-3.5 w-3.5" })]),
																						_: 1
																					}, 8, ["onClick"])) : createCommentVNode("", true), unref(can)("produk.delete") ? (openBlock(), createBlock(_sfc_main$3, {
																						key: 1,
																						variant: "ghost",
																						size: "icon",
																						class: "h-7 w-7 text-zinc-400 hover:text-destructive hover:bg-destructive/10 rounded-md",
																						title: "Hapus",
																						onClick: ($event) => doDelete(product)
																					}, {
																						default: withCtx(() => [createVNode(unref(Trash2), { class: "h-3.5 w-3.5" })]),
																						_: 1
																					}, 8, ["onClick"])) : createCommentVNode("", true)])]),
																					_: 2
																				}, 1024)
																			]),
																			_: 2
																		}, 1024);
																	}), 128))];
																}),
																_: 1
															}, _parent, _scopeId));
														} else return [createVNode(_sfc_main$12, null, {
															default: withCtx(() => [createVNode(_sfc_main$13, { class: "hover:bg-transparent border-b border-zinc-100 dark:border-zinc-800" }, {
																default: withCtx(() => [
																	createVNode(_sfc_main$14, { class: "pl-5 py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500 w-[260px]" }, {
																		default: withCtx(() => [createTextVNode("Produk")]),
																		_: 1
																	}),
																	createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
																		default: withCtx(() => [createTextVNode("SKU")]),
																		_: 1
																	}),
																	createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
																		default: withCtx(() => [createTextVNode("Harga")]),
																		_: 1
																	}),
																	createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
																		default: withCtx(() => [createTextVNode("Kategori")]),
																		_: 1
																	}),
																	createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500 text-center" }, {
																		default: withCtx(() => [createTextVNode("Stok")]),
																		_: 1
																	}),
																	createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500 text-center" }, {
																		default: withCtx(() => [createTextVNode("Status")]),
																		_: 1
																	}),
																	createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
																		default: withCtx(() => [createTextVNode("Dibuat")]),
																		_: 1
																	}),
																	createVNode(_sfc_main$14, { class: "pr-5 py-3 text-right" })
																]),
																_: 1
															})]),
															_: 1
														}), createVNode(_sfc_main$15, null, {
															default: withCtx(() => [(openBlock(true), createBlock(Fragment, null, renderList(filteredProducts.value, (product) => {
																return openBlock(), createBlock(_sfc_main$13, {
																	key: product.id,
																	class: "group border-b border-zinc-100 dark:border-zinc-800/60 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors"
																}, {
																	default: withCtx(() => [
																		createVNode(_sfc_main$16, { class: "pl-5 py-3" }, {
																			default: withCtx(() => [createVNode("div", { class: "flex items-center gap-3" }, [product.imageUrl ? (openBlock(), createBlock("img", {
																				key: 0,
																				src: product.imageUrl,
																				class: "w-8 h-8 rounded-lg object-cover border border-zinc-200 dark:border-zinc-700"
																			}, null, 8, ["src"])) : (openBlock(), createBlock("div", {
																				key: 1,
																				class: "w-8 h-8 rounded-lg flex items-center justify-center text-xs font-bold shrink-0 select-none border border-zinc-200 dark:border-zinc-700/50",
																				style: productAvatarStyle(product.name)
																			}, toDisplayString(product.name?.charAt(0).toUpperCase()), 5)), createVNode("span", { class: "font-medium text-sm text-zinc-900 dark:text-zinc-100 truncate max-w-[170px]" }, toDisplayString(product.name), 1)])]),
																			_: 2
																		}, 1024),
																		createVNode(_sfc_main$16, { class: "py-3" }, {
																			default: withCtx(() => [product.sku ? (openBlock(), createBlock("span", {
																				key: 0,
																				class: "font-mono text-xs bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-400 px-2 py-1 rounded"
																			}, toDisplayString(product.sku), 1)) : (openBlock(), createBlock("span", {
																				key: 1,
																				class: "text-zinc-300 dark:text-zinc-600 text-sm"
																			}, "—"))]),
																			_: 2
																		}, 1024),
																		createVNode(_sfc_main$16, { class: "py-3" }, {
																			default: withCtx(() => [createVNode("span", { class: "text-sm font-semibold text-zinc-800 dark:text-zinc-200" }, toDisplayString(formatCurrency(product.price)), 1)]),
																			_: 2
																		}, 1024),
																		createVNode(_sfc_main$16, { class: "py-3" }, {
																			default: withCtx(() => [product.categoryName ? (openBlock(), createBlock("span", {
																				key: 0,
																				class: "inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full bg-blue-50 dark:bg-blue-900/20 text-blue-700 dark:text-blue-300 border border-blue-100 dark:border-blue-800/40"
																			}, toDisplayString(product.categoryName), 1)) : (openBlock(), createBlock("span", {
																				key: 1,
																				class: "text-zinc-300 dark:text-zinc-600 text-sm"
																			}, "—"))]),
																			_: 2
																		}, 1024),
																		createVNode(_sfc_main$16, { class: "py-3 text-center" }, {
																			default: withCtx(() => [createVNode("span", { class: ["inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full", product.trackStock ? "bg-emerald-50 dark:bg-emerald-900/20 text-emerald-700 dark:text-emerald-400 border border-emerald-100 dark:border-emerald-800/40" : "bg-zinc-100 dark:bg-zinc-800 text-zinc-500 border border-zinc-200 dark:border-zinc-700"] }, [createVNode("span", { class: ["w-1.5 h-1.5 rounded-full", product.trackStock ? "bg-emerald-500" : "bg-zinc-400"] }, null, 2), createTextVNode(" " + toDisplayString(product.trackStock ? "Dilacak" : "Bebas"), 1)], 2)]),
																			_: 2
																		}, 1024),
																		createVNode(_sfc_main$16, { class: "py-3 text-center" }, {
																			default: withCtx(() => [createVNode("span", { class: ["inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full", product.isActive ? "bg-primary/10 text-primary border border-primary/20" : "bg-zinc-100 dark:bg-zinc-800 text-zinc-500 border border-zinc-200 dark:border-zinc-700"] }, [createVNode("span", { class: ["w-1.5 h-1.5 rounded-full", product.isActive ? "bg-primary" : "bg-zinc-400"] }, null, 2), createTextVNode(" " + toDisplayString(product.isActive ? "Aktif" : "Nonaktif"), 1)], 2)]),
																			_: 2
																		}, 1024),
																		createVNode(_sfc_main$16, { class: "py-3" }, {
																			default: withCtx(() => [createVNode("span", { class: "text-xs text-zinc-400 dark:text-zinc-500" }, toDisplayString(formatDate(product.createdAt)), 1)]),
																			_: 2
																		}, 1024),
																		createVNode(_sfc_main$16, { class: "pr-4 py-3 text-right" }, {
																			default: withCtx(() => [createVNode("div", { class: "flex justify-end gap-1 transition-opacity" }, [unref(can)("produk.update") ? (openBlock(), createBlock(_sfc_main$3, {
																				key: 0,
																				variant: "ghost",
																				size: "icon",
																				class: "h-7 w-7 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 hover:bg-zinc-100 dark:hover:bg-zinc-800 rounded-md",
																				title: "Edit",
																				onClick: ($event) => openEdit(product)
																			}, {
																				default: withCtx(() => [createVNode(unref(Pencil), { class: "h-3.5 w-3.5" })]),
																				_: 1
																			}, 8, ["onClick"])) : createCommentVNode("", true), unref(can)("produk.delete") ? (openBlock(), createBlock(_sfc_main$3, {
																				key: 1,
																				variant: "ghost",
																				size: "icon",
																				class: "h-7 w-7 text-zinc-400 hover:text-destructive hover:bg-destructive/10 rounded-md",
																				title: "Hapus",
																				onClick: ($event) => doDelete(product)
																			}, {
																				default: withCtx(() => [createVNode(unref(Trash2), { class: "h-3.5 w-3.5" })]),
																				_: 1
																			}, 8, ["onClick"])) : createCommentVNode("", true)])]),
																			_: 2
																		}, 1024)
																	]),
																	_: 2
																}, 1024);
															}), 128))]),
															_: 1
														})];
													}),
													_: 1
												}, _parent, _scopeId));
												_push(`</div></div>`);
											}
											if (pagination.value.totalElements > 0 && !loading.value) _push(ssrRenderComponent(_sfc_main$10, {
												page: pagination.value.page + 1,
												"page-size": pagination.value.size,
												total: pagination.value.totalElements,
												"onUpdate:page": ($event) => fetchProducts($event - 1),
												"onUpdate:pageSize": ($event) => updatePageSize($event)
											}, null, _parent, _scopeId));
											else _push(`<!---->`);
										} else return [loading.value ? (openBlock(), createBlock("div", {
											key: 0,
											class: "flex flex-col items-center justify-center py-24 gap-3"
										}, [createVNode(unref(Loader2), { class: "h-7 w-7 animate-spin text-primary/50" }), createVNode("p", { class: "text-xs text-muted-foreground" }, "Memuat data...")])) : filteredProducts.value.length === 0 ? (openBlock(), createBlock("div", {
											key: 1,
											class: "flex flex-col items-center justify-center py-24 text-muted-foreground"
										}, [
											createVNode("div", { class: "w-14 h-14 rounded-full bg-muted flex items-center justify-center mb-4" }, [createVNode(unref(Package), { class: "h-7 w-7 opacity-40" })]),
											createVNode("p", { class: "text-sm font-medium" }, "Belum ada produk"),
											createVNode("p", { class: "text-xs text-muted-foreground/70 mt-1" }, "Mulai dengan menambahkan produk pertama Anda."),
											unref(can)("produk.store") && !searchQuery.value ? (openBlock(), createBlock(_sfc_main$3, {
												key: 0,
												size: "sm",
												class: "mt-4",
												onClick: openCreate
											}, {
												default: withCtx(() => [createVNode(unref(Plus), { class: "h-3.5 w-3.5 mr-1.5" }), createTextVNode(" Tambah Produk ")]),
												_: 1
											})) : createCommentVNode("", true)
										])) : (openBlock(), createBlock("div", { key: 2 }, [createVNode("div", { class: "md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60" }, [(openBlock(true), createBlock(Fragment, null, renderList(filteredProducts.value, (product) => {
											return openBlock(), createBlock("div", {
												key: "mobile-" + product.id,
												class: "p-4 flex flex-col gap-3 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors"
											}, [createVNode("div", { class: "flex items-start justify-between gap-3" }, [createVNode("div", { class: "flex items-center gap-3" }, [product.imageUrl ? (openBlock(), createBlock("img", {
												key: 0,
												src: product.imageUrl,
												class: "w-12 h-12 rounded-lg object-cover border border-zinc-200 dark:border-zinc-800"
											}, null, 8, ["src"])) : (openBlock(), createBlock("div", {
												key: 1,
												class: "w-12 h-12 rounded-lg flex items-center justify-center text-sm font-bold shrink-0 select-none border border-zinc-200 dark:border-zinc-800/50",
												style: productAvatarStyle(product.name)
											}, toDisplayString(product.name?.charAt(0).toUpperCase()), 5)), createVNode("div", null, [createVNode("h4", { class: "font-medium text-sm text-zinc-900 dark:text-zinc-100 line-clamp-1" }, toDisplayString(product.name), 1), product.sku ? (openBlock(), createBlock("span", {
												key: 0,
												class: "font-mono text-[10px] bg-zinc-100 dark:bg-zinc-800 text-zinc-500 px-1.5 py-0.5 rounded mt-1 inline-block"
											}, toDisplayString(product.sku), 1)) : createCommentVNode("", true)])]), createVNode("div", { class: "flex items-center gap-1 shrink-0" }, [unref(can)("produk.update") ? (openBlock(), createBlock(_sfc_main$3, {
												key: 0,
												variant: "ghost",
												size: "icon",
												class: "h-8 w-8 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 bg-zinc-50 dark:bg-zinc-800/50",
												onClick: ($event) => openEdit(product)
											}, {
												default: withCtx(() => [createVNode(unref(Pencil), { class: "h-3.5 w-3.5" })]),
												_: 1
											}, 8, ["onClick"])) : createCommentVNode("", true), unref(can)("produk.delete") ? (openBlock(), createBlock(_sfc_main$3, {
												key: 1,
												variant: "ghost",
												size: "icon",
												class: "h-8 w-8 text-zinc-400 hover:text-destructive bg-zinc-50 dark:bg-zinc-800/50",
												onClick: ($event) => doDelete(product)
											}, {
												default: withCtx(() => [createVNode(unref(Trash2), { class: "h-3.5 w-3.5" })]),
												_: 1
											}, 8, ["onClick"])) : createCommentVNode("", true)])]), createVNode("div", { class: "flex items-center justify-between mt-1" }, [createVNode("span", { class: "text-sm font-semibold text-zinc-800 dark:text-zinc-200" }, toDisplayString(formatCurrency(product.price)), 1), createVNode("div", { class: "flex items-center gap-2" }, [product.categoryName ? (openBlock(), createBlock("span", {
												key: 0,
												class: "text-[10px] font-medium px-2 py-0.5 rounded-full bg-blue-50 text-blue-700 dark:bg-blue-900/20 dark:text-blue-300 border border-blue-100 dark:border-blue-800/40"
											}, toDisplayString(product.categoryName), 1)) : createCommentVNode("", true), createVNode("span", { class: ["inline-flex items-center gap-1 text-[10px] font-medium px-2 py-0.5 rounded-full", product.isActive ? "bg-primary/10 text-primary border border-primary/20" : "bg-zinc-100 dark:bg-zinc-800 text-zinc-500 border border-zinc-200 dark:border-zinc-700"] }, [createVNode("span", { class: ["w-1 h-1 rounded-full", product.isActive ? "bg-primary" : "bg-zinc-400"] }, null, 2), createTextVNode(" " + toDisplayString(product.isActive ? "Aktif" : "Nonaktif"), 1)], 2)])])]);
										}), 128))]), createVNode("div", { class: "hidden md:block overflow-x-auto" }, [createVNode(_sfc_main$11, null, {
											default: withCtx(() => [createVNode(_sfc_main$12, null, {
												default: withCtx(() => [createVNode(_sfc_main$13, { class: "hover:bg-transparent border-b border-zinc-100 dark:border-zinc-800" }, {
													default: withCtx(() => [
														createVNode(_sfc_main$14, { class: "pl-5 py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500 w-[260px]" }, {
															default: withCtx(() => [createTextVNode("Produk")]),
															_: 1
														}),
														createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
															default: withCtx(() => [createTextVNode("SKU")]),
															_: 1
														}),
														createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
															default: withCtx(() => [createTextVNode("Harga")]),
															_: 1
														}),
														createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
															default: withCtx(() => [createTextVNode("Kategori")]),
															_: 1
														}),
														createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500 text-center" }, {
															default: withCtx(() => [createTextVNode("Stok")]),
															_: 1
														}),
														createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500 text-center" }, {
															default: withCtx(() => [createTextVNode("Status")]),
															_: 1
														}),
														createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
															default: withCtx(() => [createTextVNode("Dibuat")]),
															_: 1
														}),
														createVNode(_sfc_main$14, { class: "pr-5 py-3 text-right" })
													]),
													_: 1
												})]),
												_: 1
											}), createVNode(_sfc_main$15, null, {
												default: withCtx(() => [(openBlock(true), createBlock(Fragment, null, renderList(filteredProducts.value, (product) => {
													return openBlock(), createBlock(_sfc_main$13, {
														key: product.id,
														class: "group border-b border-zinc-100 dark:border-zinc-800/60 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors"
													}, {
														default: withCtx(() => [
															createVNode(_sfc_main$16, { class: "pl-5 py-3" }, {
																default: withCtx(() => [createVNode("div", { class: "flex items-center gap-3" }, [product.imageUrl ? (openBlock(), createBlock("img", {
																	key: 0,
																	src: product.imageUrl,
																	class: "w-8 h-8 rounded-lg object-cover border border-zinc-200 dark:border-zinc-700"
																}, null, 8, ["src"])) : (openBlock(), createBlock("div", {
																	key: 1,
																	class: "w-8 h-8 rounded-lg flex items-center justify-center text-xs font-bold shrink-0 select-none border border-zinc-200 dark:border-zinc-700/50",
																	style: productAvatarStyle(product.name)
																}, toDisplayString(product.name?.charAt(0).toUpperCase()), 5)), createVNode("span", { class: "font-medium text-sm text-zinc-900 dark:text-zinc-100 truncate max-w-[170px]" }, toDisplayString(product.name), 1)])]),
																_: 2
															}, 1024),
															createVNode(_sfc_main$16, { class: "py-3" }, {
																default: withCtx(() => [product.sku ? (openBlock(), createBlock("span", {
																	key: 0,
																	class: "font-mono text-xs bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-400 px-2 py-1 rounded"
																}, toDisplayString(product.sku), 1)) : (openBlock(), createBlock("span", {
																	key: 1,
																	class: "text-zinc-300 dark:text-zinc-600 text-sm"
																}, "—"))]),
																_: 2
															}, 1024),
															createVNode(_sfc_main$16, { class: "py-3" }, {
																default: withCtx(() => [createVNode("span", { class: "text-sm font-semibold text-zinc-800 dark:text-zinc-200" }, toDisplayString(formatCurrency(product.price)), 1)]),
																_: 2
															}, 1024),
															createVNode(_sfc_main$16, { class: "py-3" }, {
																default: withCtx(() => [product.categoryName ? (openBlock(), createBlock("span", {
																	key: 0,
																	class: "inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full bg-blue-50 dark:bg-blue-900/20 text-blue-700 dark:text-blue-300 border border-blue-100 dark:border-blue-800/40"
																}, toDisplayString(product.categoryName), 1)) : (openBlock(), createBlock("span", {
																	key: 1,
																	class: "text-zinc-300 dark:text-zinc-600 text-sm"
																}, "—"))]),
																_: 2
															}, 1024),
															createVNode(_sfc_main$16, { class: "py-3 text-center" }, {
																default: withCtx(() => [createVNode("span", { class: ["inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full", product.trackStock ? "bg-emerald-50 dark:bg-emerald-900/20 text-emerald-700 dark:text-emerald-400 border border-emerald-100 dark:border-emerald-800/40" : "bg-zinc-100 dark:bg-zinc-800 text-zinc-500 border border-zinc-200 dark:border-zinc-700"] }, [createVNode("span", { class: ["w-1.5 h-1.5 rounded-full", product.trackStock ? "bg-emerald-500" : "bg-zinc-400"] }, null, 2), createTextVNode(" " + toDisplayString(product.trackStock ? "Dilacak" : "Bebas"), 1)], 2)]),
																_: 2
															}, 1024),
															createVNode(_sfc_main$16, { class: "py-3 text-center" }, {
																default: withCtx(() => [createVNode("span", { class: ["inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full", product.isActive ? "bg-primary/10 text-primary border border-primary/20" : "bg-zinc-100 dark:bg-zinc-800 text-zinc-500 border border-zinc-200 dark:border-zinc-700"] }, [createVNode("span", { class: ["w-1.5 h-1.5 rounded-full", product.isActive ? "bg-primary" : "bg-zinc-400"] }, null, 2), createTextVNode(" " + toDisplayString(product.isActive ? "Aktif" : "Nonaktif"), 1)], 2)]),
																_: 2
															}, 1024),
															createVNode(_sfc_main$16, { class: "py-3" }, {
																default: withCtx(() => [createVNode("span", { class: "text-xs text-zinc-400 dark:text-zinc-500" }, toDisplayString(formatDate(product.createdAt)), 1)]),
																_: 2
															}, 1024),
															createVNode(_sfc_main$16, { class: "pr-4 py-3 text-right" }, {
																default: withCtx(() => [createVNode("div", { class: "flex justify-end gap-1 transition-opacity" }, [unref(can)("produk.update") ? (openBlock(), createBlock(_sfc_main$3, {
																	key: 0,
																	variant: "ghost",
																	size: "icon",
																	class: "h-7 w-7 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 hover:bg-zinc-100 dark:hover:bg-zinc-800 rounded-md",
																	title: "Edit",
																	onClick: ($event) => openEdit(product)
																}, {
																	default: withCtx(() => [createVNode(unref(Pencil), { class: "h-3.5 w-3.5" })]),
																	_: 1
																}, 8, ["onClick"])) : createCommentVNode("", true), unref(can)("produk.delete") ? (openBlock(), createBlock(_sfc_main$3, {
																	key: 1,
																	variant: "ghost",
																	size: "icon",
																	class: "h-7 w-7 text-zinc-400 hover:text-destructive hover:bg-destructive/10 rounded-md",
																	title: "Hapus",
																	onClick: ($event) => doDelete(product)
																}, {
																	default: withCtx(() => [createVNode(unref(Trash2), { class: "h-3.5 w-3.5" })]),
																	_: 1
																}, 8, ["onClick"])) : createCommentVNode("", true)])]),
																_: 2
															}, 1024)
														]),
														_: 2
													}, 1024);
												}), 128))]),
												_: 1
											})]),
											_: 1
										})])])), pagination.value.totalElements > 0 && !loading.value ? (openBlock(), createBlock(_sfc_main$10, {
											key: 3,
											page: pagination.value.page + 1,
											"page-size": pagination.value.size,
											total: pagination.value.totalElements,
											"onUpdate:page": ($event) => fetchProducts($event - 1),
											"onUpdate:pageSize": ($event) => updatePageSize($event)
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
								else return [createVNode(_sfc_main$5, { class: "p-0" }, {
									default: withCtx(() => [loading.value ? (openBlock(), createBlock("div", {
										key: 0,
										class: "flex flex-col items-center justify-center py-24 gap-3"
									}, [createVNode(unref(Loader2), { class: "h-7 w-7 animate-spin text-primary/50" }), createVNode("p", { class: "text-xs text-muted-foreground" }, "Memuat data...")])) : filteredProducts.value.length === 0 ? (openBlock(), createBlock("div", {
										key: 1,
										class: "flex flex-col items-center justify-center py-24 text-muted-foreground"
									}, [
										createVNode("div", { class: "w-14 h-14 rounded-full bg-muted flex items-center justify-center mb-4" }, [createVNode(unref(Package), { class: "h-7 w-7 opacity-40" })]),
										createVNode("p", { class: "text-sm font-medium" }, "Belum ada produk"),
										createVNode("p", { class: "text-xs text-muted-foreground/70 mt-1" }, "Mulai dengan menambahkan produk pertama Anda."),
										unref(can)("produk.store") && !searchQuery.value ? (openBlock(), createBlock(_sfc_main$3, {
											key: 0,
											size: "sm",
											class: "mt-4",
											onClick: openCreate
										}, {
											default: withCtx(() => [createVNode(unref(Plus), { class: "h-3.5 w-3.5 mr-1.5" }), createTextVNode(" Tambah Produk ")]),
											_: 1
										})) : createCommentVNode("", true)
									])) : (openBlock(), createBlock("div", { key: 2 }, [createVNode("div", { class: "md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60" }, [(openBlock(true), createBlock(Fragment, null, renderList(filteredProducts.value, (product) => {
										return openBlock(), createBlock("div", {
											key: "mobile-" + product.id,
											class: "p-4 flex flex-col gap-3 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors"
										}, [createVNode("div", { class: "flex items-start justify-between gap-3" }, [createVNode("div", { class: "flex items-center gap-3" }, [product.imageUrl ? (openBlock(), createBlock("img", {
											key: 0,
											src: product.imageUrl,
											class: "w-12 h-12 rounded-lg object-cover border border-zinc-200 dark:border-zinc-800"
										}, null, 8, ["src"])) : (openBlock(), createBlock("div", {
											key: 1,
											class: "w-12 h-12 rounded-lg flex items-center justify-center text-sm font-bold shrink-0 select-none border border-zinc-200 dark:border-zinc-800/50",
											style: productAvatarStyle(product.name)
										}, toDisplayString(product.name?.charAt(0).toUpperCase()), 5)), createVNode("div", null, [createVNode("h4", { class: "font-medium text-sm text-zinc-900 dark:text-zinc-100 line-clamp-1" }, toDisplayString(product.name), 1), product.sku ? (openBlock(), createBlock("span", {
											key: 0,
											class: "font-mono text-[10px] bg-zinc-100 dark:bg-zinc-800 text-zinc-500 px-1.5 py-0.5 rounded mt-1 inline-block"
										}, toDisplayString(product.sku), 1)) : createCommentVNode("", true)])]), createVNode("div", { class: "flex items-center gap-1 shrink-0" }, [unref(can)("produk.update") ? (openBlock(), createBlock(_sfc_main$3, {
											key: 0,
											variant: "ghost",
											size: "icon",
											class: "h-8 w-8 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 bg-zinc-50 dark:bg-zinc-800/50",
											onClick: ($event) => openEdit(product)
										}, {
											default: withCtx(() => [createVNode(unref(Pencil), { class: "h-3.5 w-3.5" })]),
											_: 1
										}, 8, ["onClick"])) : createCommentVNode("", true), unref(can)("produk.delete") ? (openBlock(), createBlock(_sfc_main$3, {
											key: 1,
											variant: "ghost",
											size: "icon",
											class: "h-8 w-8 text-zinc-400 hover:text-destructive bg-zinc-50 dark:bg-zinc-800/50",
											onClick: ($event) => doDelete(product)
										}, {
											default: withCtx(() => [createVNode(unref(Trash2), { class: "h-3.5 w-3.5" })]),
											_: 1
										}, 8, ["onClick"])) : createCommentVNode("", true)])]), createVNode("div", { class: "flex items-center justify-between mt-1" }, [createVNode("span", { class: "text-sm font-semibold text-zinc-800 dark:text-zinc-200" }, toDisplayString(formatCurrency(product.price)), 1), createVNode("div", { class: "flex items-center gap-2" }, [product.categoryName ? (openBlock(), createBlock("span", {
											key: 0,
											class: "text-[10px] font-medium px-2 py-0.5 rounded-full bg-blue-50 text-blue-700 dark:bg-blue-900/20 dark:text-blue-300 border border-blue-100 dark:border-blue-800/40"
										}, toDisplayString(product.categoryName), 1)) : createCommentVNode("", true), createVNode("span", { class: ["inline-flex items-center gap-1 text-[10px] font-medium px-2 py-0.5 rounded-full", product.isActive ? "bg-primary/10 text-primary border border-primary/20" : "bg-zinc-100 dark:bg-zinc-800 text-zinc-500 border border-zinc-200 dark:border-zinc-700"] }, [createVNode("span", { class: ["w-1 h-1 rounded-full", product.isActive ? "bg-primary" : "bg-zinc-400"] }, null, 2), createTextVNode(" " + toDisplayString(product.isActive ? "Aktif" : "Nonaktif"), 1)], 2)])])]);
									}), 128))]), createVNode("div", { class: "hidden md:block overflow-x-auto" }, [createVNode(_sfc_main$11, null, {
										default: withCtx(() => [createVNode(_sfc_main$12, null, {
											default: withCtx(() => [createVNode(_sfc_main$13, { class: "hover:bg-transparent border-b border-zinc-100 dark:border-zinc-800" }, {
												default: withCtx(() => [
													createVNode(_sfc_main$14, { class: "pl-5 py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500 w-[260px]" }, {
														default: withCtx(() => [createTextVNode("Produk")]),
														_: 1
													}),
													createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
														default: withCtx(() => [createTextVNode("SKU")]),
														_: 1
													}),
													createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
														default: withCtx(() => [createTextVNode("Harga")]),
														_: 1
													}),
													createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
														default: withCtx(() => [createTextVNode("Kategori")]),
														_: 1
													}),
													createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500 text-center" }, {
														default: withCtx(() => [createTextVNode("Stok")]),
														_: 1
													}),
													createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500 text-center" }, {
														default: withCtx(() => [createTextVNode("Status")]),
														_: 1
													}),
													createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
														default: withCtx(() => [createTextVNode("Dibuat")]),
														_: 1
													}),
													createVNode(_sfc_main$14, { class: "pr-5 py-3 text-right" })
												]),
												_: 1
											})]),
											_: 1
										}), createVNode(_sfc_main$15, null, {
											default: withCtx(() => [(openBlock(true), createBlock(Fragment, null, renderList(filteredProducts.value, (product) => {
												return openBlock(), createBlock(_sfc_main$13, {
													key: product.id,
													class: "group border-b border-zinc-100 dark:border-zinc-800/60 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors"
												}, {
													default: withCtx(() => [
														createVNode(_sfc_main$16, { class: "pl-5 py-3" }, {
															default: withCtx(() => [createVNode("div", { class: "flex items-center gap-3" }, [product.imageUrl ? (openBlock(), createBlock("img", {
																key: 0,
																src: product.imageUrl,
																class: "w-8 h-8 rounded-lg object-cover border border-zinc-200 dark:border-zinc-700"
															}, null, 8, ["src"])) : (openBlock(), createBlock("div", {
																key: 1,
																class: "w-8 h-8 rounded-lg flex items-center justify-center text-xs font-bold shrink-0 select-none border border-zinc-200 dark:border-zinc-700/50",
																style: productAvatarStyle(product.name)
															}, toDisplayString(product.name?.charAt(0).toUpperCase()), 5)), createVNode("span", { class: "font-medium text-sm text-zinc-900 dark:text-zinc-100 truncate max-w-[170px]" }, toDisplayString(product.name), 1)])]),
															_: 2
														}, 1024),
														createVNode(_sfc_main$16, { class: "py-3" }, {
															default: withCtx(() => [product.sku ? (openBlock(), createBlock("span", {
																key: 0,
																class: "font-mono text-xs bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-400 px-2 py-1 rounded"
															}, toDisplayString(product.sku), 1)) : (openBlock(), createBlock("span", {
																key: 1,
																class: "text-zinc-300 dark:text-zinc-600 text-sm"
															}, "—"))]),
															_: 2
														}, 1024),
														createVNode(_sfc_main$16, { class: "py-3" }, {
															default: withCtx(() => [createVNode("span", { class: "text-sm font-semibold text-zinc-800 dark:text-zinc-200" }, toDisplayString(formatCurrency(product.price)), 1)]),
															_: 2
														}, 1024),
														createVNode(_sfc_main$16, { class: "py-3" }, {
															default: withCtx(() => [product.categoryName ? (openBlock(), createBlock("span", {
																key: 0,
																class: "inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full bg-blue-50 dark:bg-blue-900/20 text-blue-700 dark:text-blue-300 border border-blue-100 dark:border-blue-800/40"
															}, toDisplayString(product.categoryName), 1)) : (openBlock(), createBlock("span", {
																key: 1,
																class: "text-zinc-300 dark:text-zinc-600 text-sm"
															}, "—"))]),
															_: 2
														}, 1024),
														createVNode(_sfc_main$16, { class: "py-3 text-center" }, {
															default: withCtx(() => [createVNode("span", { class: ["inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full", product.trackStock ? "bg-emerald-50 dark:bg-emerald-900/20 text-emerald-700 dark:text-emerald-400 border border-emerald-100 dark:border-emerald-800/40" : "bg-zinc-100 dark:bg-zinc-800 text-zinc-500 border border-zinc-200 dark:border-zinc-700"] }, [createVNode("span", { class: ["w-1.5 h-1.5 rounded-full", product.trackStock ? "bg-emerald-500" : "bg-zinc-400"] }, null, 2), createTextVNode(" " + toDisplayString(product.trackStock ? "Dilacak" : "Bebas"), 1)], 2)]),
															_: 2
														}, 1024),
														createVNode(_sfc_main$16, { class: "py-3 text-center" }, {
															default: withCtx(() => [createVNode("span", { class: ["inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full", product.isActive ? "bg-primary/10 text-primary border border-primary/20" : "bg-zinc-100 dark:bg-zinc-800 text-zinc-500 border border-zinc-200 dark:border-zinc-700"] }, [createVNode("span", { class: ["w-1.5 h-1.5 rounded-full", product.isActive ? "bg-primary" : "bg-zinc-400"] }, null, 2), createTextVNode(" " + toDisplayString(product.isActive ? "Aktif" : "Nonaktif"), 1)], 2)]),
															_: 2
														}, 1024),
														createVNode(_sfc_main$16, { class: "py-3" }, {
															default: withCtx(() => [createVNode("span", { class: "text-xs text-zinc-400 dark:text-zinc-500" }, toDisplayString(formatDate(product.createdAt)), 1)]),
															_: 2
														}, 1024),
														createVNode(_sfc_main$16, { class: "pr-4 py-3 text-right" }, {
															default: withCtx(() => [createVNode("div", { class: "flex justify-end gap-1 transition-opacity" }, [unref(can)("produk.update") ? (openBlock(), createBlock(_sfc_main$3, {
																key: 0,
																variant: "ghost",
																size: "icon",
																class: "h-7 w-7 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 hover:bg-zinc-100 dark:hover:bg-zinc-800 rounded-md",
																title: "Edit",
																onClick: ($event) => openEdit(product)
															}, {
																default: withCtx(() => [createVNode(unref(Pencil), { class: "h-3.5 w-3.5" })]),
																_: 1
															}, 8, ["onClick"])) : createCommentVNode("", true), unref(can)("produk.delete") ? (openBlock(), createBlock(_sfc_main$3, {
																key: 1,
																variant: "ghost",
																size: "icon",
																class: "h-7 w-7 text-zinc-400 hover:text-destructive hover:bg-destructive/10 rounded-md",
																title: "Hapus",
																onClick: ($event) => doDelete(product)
															}, {
																default: withCtx(() => [createVNode(unref(Trash2), { class: "h-3.5 w-3.5" })]),
																_: 1
															}, 8, ["onClick"])) : createCommentVNode("", true)])]),
															_: 2
														}, 1024)
													]),
													_: 2
												}, 1024);
											}), 128))]),
											_: 1
										})]),
										_: 1
									})])])), pagination.value.totalElements > 0 && !loading.value ? (openBlock(), createBlock(_sfc_main$10, {
										key: 3,
										page: pagination.value.page + 1,
										"page-size": pagination.value.size,
										total: pagination.value.totalElements,
										"onUpdate:page": ($event) => fetchProducts($event - 1),
										"onUpdate:pageSize": ($event) => updatePageSize($event)
									}, null, 8, [
										"page",
										"page-size",
										"total",
										"onUpdate:page",
										"onUpdate:pageSize"
									])) : createCommentVNode("", true)]),
									_: 1
								})];
							}),
							_: 1
						}, _parent, _scopeId));
						_push(`</div>`);
						ssrRenderTeleport(_push, (_push) => {
							if (showDrawer.value) _push(`<div class="fixed inset-0 z-[50] bg-black/40 backdrop-blur-sm" data-v-11961a06${_scopeId}></div>`);
							else _push(`<!---->`);
							if (showDrawer.value) {
								_push(`<div class="fixed inset-y-0 right-0 z-[50] flex flex-col w-full sm:max-w-[420px] h-full bg-card shadow-2xl sm:border-l overflow-hidden" data-v-11961a06${_scopeId}><div class="flex items-center justify-between px-6 py-4 border-b shrink-0" data-v-11961a06${_scopeId}><div data-v-11961a06${_scopeId}><h3 class="font-semibold text-base" data-v-11961a06${_scopeId}>${ssrInterpolate(modalMode.value === "create" ? "Tambah Produk" : "Edit Produk")}</h3><p class="text-xs text-muted-foreground mt-0.5" data-v-11961a06${_scopeId}>${ssrInterpolate(modalMode.value === "create" ? "Isi detail produk baru." : "Perbarui informasi produk.")}</p></div>`);
								_push(ssrRenderComponent(_sfc_main$3, {
									variant: "ghost",
									size: "icon",
									onClick: closeDrawer
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(ssrRenderComponent(unref(X), { class: "h-4 w-4" }, null, _parent, _scopeId));
										else return [createVNode(unref(X), { class: "h-4 w-4" })];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`</div><div class="flex-1 overflow-y-auto px-6 py-5 space-y-5" data-v-11961a06${_scopeId}>`);
								if (formError.value) _push(ssrRenderComponent(_sfc_main$7, { variant: "destructive" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`<p class="text-sm" data-v-11961a06${_scopeId}>${ssrInterpolate(formError.value)}</p>`);
										else return [createVNode("p", { class: "text-sm" }, toDisplayString(formError.value), 1)];
									}),
									_: 1
								}, _parent, _scopeId));
								else _push(`<!---->`);
								_push(`<div class="space-y-1.5" data-v-11961a06${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$8, null, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Foto Produk`);
										else return [createTextVNode("Foto Produk")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`<div class="flex items-center gap-4" data-v-11961a06${_scopeId}><div class="h-20 w-20 shrink-0 overflow-hidden rounded-lg border border-dashed border-zinc-300 dark:border-zinc-700 bg-zinc-50 dark:bg-zinc-900 flex items-center justify-center relative" data-v-11961a06${_scopeId}>`);
								if (form.value.imagePreview) _push(`<img${ssrRenderAttr("src", form.value.imagePreview)} alt="Preview" class="h-full w-full object-cover" data-v-11961a06${_scopeId}>`);
								else _push(ssrRenderComponent(unref(Package), { class: "h-8 w-8 text-zinc-400" }, null, _parent, _scopeId));
								_push(`<input type="file" accept="image/*" class="absolute inset-0 w-full h-full opacity-0 cursor-pointer"${ssrIncludeBooleanAttr(saving.value) ? " disabled" : ""} data-v-11961a06${_scopeId}></div><div class="flex flex-col gap-2" data-v-11961a06${_scopeId}><div class="relative" data-v-11961a06${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$3, {
									type: "button",
									variant: "outline",
									size: "sm"
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) {
											_push(ssrRenderComponent(unref(Upload), { class: "h-3.5 w-3.5 mr-2" }, null, _parent, _scopeId));
											_push(` Pilih Foto `);
										} else return [createVNode(unref(Upload), { class: "h-3.5 w-3.5 mr-2" }), createTextVNode(" Pilih Foto ")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`<input type="file" accept="image/*" class="absolute inset-0 w-full h-full opacity-0 cursor-pointer"${ssrIncludeBooleanAttr(saving.value) ? " disabled" : ""} data-v-11961a06${_scopeId}></div><p class="text-[10px] text-muted-foreground" data-v-11961a06${_scopeId}>Format: JPG, PNG. Maks 2MB.</p></div></div></div><div class="space-y-1.5" data-v-11961a06${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$8, { for: "f-name" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Nama Produk <span class="text-destructive" data-v-11961a06${_scopeId}>*</span>`);
										else return [createTextVNode("Nama Produk "), createVNode("span", { class: "text-destructive" }, "*")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$6, {
									id: "f-name",
									modelValue: form.value.name,
									"onUpdate:modelValue": ($event) => form.value.name = $event,
									placeholder: "Contoh: Kaos Polos Putih",
									disabled: saving.value
								}, null, _parent, _scopeId));
								_push(`</div><div class="space-y-1.5" data-v-11961a06${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$8, { for: "f-sku" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`SKU <span class="text-muted-foreground text-xs" data-v-11961a06${_scopeId}>(opsional)</span>`);
										else return [createTextVNode("SKU "), createVNode("span", { class: "text-muted-foreground text-xs" }, "(opsional)")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$6, {
									id: "f-sku",
									modelValue: form.value.sku,
									"onUpdate:modelValue": ($event) => form.value.sku = $event,
									placeholder: "Contoh: KPP-001",
									disabled: saving.value
								}, null, _parent, _scopeId));
								_push(`</div><div class="space-y-1.5" data-v-11961a06${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$8, { for: "f-price" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Harga <span class="text-destructive" data-v-11961a06${_scopeId}>*</span>`);
										else return [createTextVNode("Harga "), createVNode("span", { class: "text-destructive" }, "*")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`<div class="relative" data-v-11961a06${_scopeId}><span class="absolute left-3 top-1/2 -translate-y-1/2 text-sm text-muted-foreground" data-v-11961a06${_scopeId}>Rp</span>`);
								_push(ssrRenderComponent(_sfc_main$6, {
									id: "f-price",
									modelValue: form.value.price,
									"onUpdate:modelValue": ($event) => form.value.price = $event,
									type: "number",
									min: "0",
									step: "100",
									placeholder: "0",
									disabled: saving.value,
									class: "pl-9"
								}, null, _parent, _scopeId));
								_push(`</div></div><div class="space-y-1.5" data-v-11961a06${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$8, { for: "f-category" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Kategori <span class="text-muted-foreground text-xs" data-v-11961a06${_scopeId}>(opsional)</span>`);
										else return [createTextVNode("Kategori "), createVNode("span", { class: "text-muted-foreground text-xs" }, "(opsional)")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`<select id="f-category"${ssrIncludeBooleanAttr(saving.value) ? " disabled" : ""} class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50" data-v-11961a06${_scopeId}><option${ssrRenderAttr("value", null)} data-v-11961a06${ssrIncludeBooleanAttr(Array.isArray(form.value.categoryId) ? ssrLooseContain(form.value.categoryId, null) : ssrLooseEqual(form.value.categoryId, null)) ? " selected" : ""}${_scopeId}>— Pilih kategori —</option><!--[-->`);
								ssrRenderList(categories.value, (cat) => {
									_push(`<option${ssrRenderAttr("value", cat.id)} data-v-11961a06${ssrIncludeBooleanAttr(Array.isArray(form.value.categoryId) ? ssrLooseContain(form.value.categoryId, cat.id) : ssrLooseEqual(form.value.categoryId, cat.id)) ? " selected" : ""}${_scopeId}>${ssrInterpolate(cat.name)}</option>`);
								});
								_push(`<!--]--></select></div><div class="flex items-center justify-between rounded-lg border p-4" data-v-11961a06${_scopeId}><div class="space-y-0.5" data-v-11961a06${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$8, {
									class: "text-sm font-medium cursor-pointer",
									for: "f-track-stock"
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Lacak Stok`);
										else return [createTextVNode("Lacak Stok")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`<p class="text-xs text-muted-foreground" data-v-11961a06${_scopeId}>Nonaktifkan untuk produk layanan.</p></div><button id="f-track-stock" type="button" role="switch"${ssrRenderAttr("aria-checked", form.value.trackStock)}${ssrIncludeBooleanAttr(saving.value) ? " disabled" : ""} class="${ssrRenderClass([form.value.trackStock ? "bg-primary" : "bg-input", "relative inline-flex h-6 w-11 shrink-0 cursor-pointer items-center rounded-full border-2 border-transparent transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"])}" data-v-11961a06${_scopeId}><span class="${ssrRenderClass([form.value.trackStock ? "translate-x-5" : "translate-x-0", "pointer-events-none block h-5 w-5 rounded-full bg-background shadow-lg ring-0 transition-transform"])}" data-v-11961a06${_scopeId}></span></button></div><div class="flex items-center justify-between rounded-lg border p-4" data-v-11961a06${_scopeId}><div class="space-y-0.5" data-v-11961a06${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$8, {
									class: "text-sm font-medium cursor-pointer",
									for: "f-is-active"
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Produk Aktif`);
										else return [createTextVNode("Produk Aktif")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`<p class="text-xs text-muted-foreground" data-v-11961a06${_scopeId}>Produk nonaktif disembunyikan.</p></div><button id="f-is-active" type="button" role="switch"${ssrRenderAttr("aria-checked", form.value.isActive)}${ssrIncludeBooleanAttr(saving.value) ? " disabled" : ""} class="${ssrRenderClass([form.value.isActive ? "bg-primary" : "bg-input", "relative inline-flex h-6 w-11 shrink-0 cursor-pointer items-center rounded-full border-2 border-transparent transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"])}" data-v-11961a06${_scopeId}><span class="${ssrRenderClass([form.value.isActive ? "translate-x-5" : "translate-x-0", "pointer-events-none block h-5 w-5 rounded-full bg-background shadow-lg ring-0 transition-transform"])}" data-v-11961a06${_scopeId}></span></button></div></div><div class="flex justify-end gap-3 px-6 py-4 border-t shrink-0 bg-muted/30" data-v-11961a06${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$3, {
									variant: "outline",
									onClick: closeDrawer,
									disabled: saving.value
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Batal`);
										else return [createTextVNode("Batal")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$3, {
									onClick: saveProduct,
									disabled: saving.value
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) {
											if (saving.value) _push(ssrRenderComponent(unref(Loader2), { class: "h-4 w-4 mr-2 animate-spin" }, null, _parent, _scopeId));
											else _push(`<!---->`);
											_push(` ${ssrInterpolate(modalMode.value === "create" ? "Simpan Produk" : "Perbarui")}`);
										} else return [saving.value ? (openBlock(), createBlock(unref(Loader2), {
											key: 0,
											class: "h-4 w-4 mr-2 animate-spin"
										})) : createCommentVNode("", true), createTextVNode(" " + toDisplayString(modalMode.value === "create" ? "Simpan Produk" : "Perbarui"), 1)];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`</div></div>`);
							} else _push(`<!---->`);
							if (deleteModal.value.show) _push(`<div class="fixed inset-0 z-[60] bg-black/40 backdrop-blur-sm" data-v-11961a06${_scopeId}></div>`);
							else _push(`<!---->`);
							if (deleteModal.value.show) {
								_push(`<div class="fixed inset-0 z-[60] flex items-center justify-center p-4 pointer-events-none" data-v-11961a06${_scopeId}><div class="relative bg-card rounded-xl shadow-2xl w-full max-w-md overflow-hidden border border-border pointer-events-auto" data-v-11961a06${_scopeId}><div class="p-6" data-v-11961a06${_scopeId}><h3 class="text-lg font-semibold text-destructive flex items-center gap-2" data-v-11961a06${_scopeId}>`);
								_push(ssrRenderComponent(unref(Trash2), { class: "h-5 w-5" }, null, _parent, _scopeId));
								_push(` Hapus Produk </h3><p class="text-sm text-muted-foreground mt-2" data-v-11961a06${_scopeId}> Tindakan ini tidak dapat dibatalkan. Hal ini akan menghapus produk <span class="font-semibold text-foreground" data-v-11961a06${_scopeId}>${ssrInterpolate(deleteModal.value.product?.name)}</span> secara permanen. </p><div class="mt-4" data-v-11961a06${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$8, { class: "text-sm font-medium" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(` Ketik <span class="font-bold select-all bg-muted px-1.5 py-0.5 rounded text-foreground" data-v-11961a06${_scopeId}>${ssrInterpolate(deleteModal.value.product?.name)}</span> untuk mengonfirmasi. `);
										else return [
											createTextVNode(" Ketik "),
											createVNode("span", { class: "font-bold select-all bg-muted px-1.5 py-0.5 rounded text-foreground" }, toDisplayString(deleteModal.value.product?.name), 1),
											createTextVNode(" untuk mengonfirmasi. ")
										];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$6, {
									modelValue: deleteModal.value.confirmText,
									"onUpdate:modelValue": ($event) => deleteModal.value.confirmText = $event,
									class: "mt-2",
									placeholder: "Masukkan nama produk"
								}, null, _parent, _scopeId));
								_push(`</div></div><div class="flex items-center justify-end gap-3 px-6 py-4 bg-muted/30 border-t" data-v-11961a06${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$3, {
									variant: "outline",
									onClick: closeDeleteModal
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Batal`);
										else return [createTextVNode("Batal")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$3, {
									variant: "destructive",
									disabled: deleteModal.value.confirmText !== deleteModal.value.product?.name || deleting.value,
									onClick: confirmDelete
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) {
											if (deleting.value) _push(ssrRenderComponent(unref(Loader2), { class: "h-4 w-4 mr-2 animate-spin" }, null, _parent, _scopeId));
											else _push(`<!---->`);
											_push(` Hapus Sekarang `);
										} else return [deleting.value ? (openBlock(), createBlock(unref(Loader2), {
											key: 0,
											class: "h-4 w-4 mr-2 animate-spin"
										})) : createCommentVNode("", true), createTextVNode(" Hapus Sekarang ")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`</div></div></div>`);
							} else _push(`<!---->`);
						}, "body", false, _parent);
					} else return [createVNode("div", { class: "flex flex-col gap-6 p-4 sm:p-6 pb-20" }, [
						createVNode("div", { class: "flex flex-col gap-4 md:flex-row md:items-start md:justify-between" }, [createVNode("div", null, [createVNode("h1", { class: "text-2xl font-bold tracking-tight" }, "Produk"), createVNode("p", { class: "text-muted-foreground text-sm mt-1" }, " Kelola data produk Anda. ")]), createVNode("div", { class: "flex flex-col sm:flex-row items-center gap-3 w-full md:w-auto" }, [createVNode("div", { class: "w-full sm:w-72" }, [createVNode(_sfc_main$9, {
							modelValue: searchQuery.value,
							"onUpdate:modelValue": ($event) => searchQuery.value = $event,
							placeholder: "Cari produk..."
						}, null, 8, ["modelValue", "onUpdate:modelValue"])]), createVNode("div", { class: "flex items-center gap-2 w-full sm:w-auto" }, [createVNode("div", {
							ref_key: "filterRef",
							ref: filterRef,
							class: "relative flex-1 sm:flex-none"
						}, [createVNode("button", {
							onClick: ($event) => showFilter.value = !showFilter.value,
							class: "w-full flex items-center justify-center gap-2 h-9 px-3 rounded-md border border-border bg-background hover:bg-accent text-foreground text-sm font-medium transition-colors"
						}, [
							createVNode(unref(Filter), { class: "h-3.5 w-3.5" }),
							createVNode("span", null, "Filter"),
							activeFilterCount.value > 0 ? (openBlock(), createBlock("span", {
								key: 0,
								class: "inline-flex items-center justify-center h-4.5 min-w-[18px] px-1 rounded-full bg-primary text-primary-foreground text-[10px] font-bold leading-none"
							}, toDisplayString(activeFilterCount.value), 1)) : createCommentVNode("", true),
							createVNode(unref(ChevronDown), {
								class: ["h-3 w-3 text-muted-foreground", showFilter.value ? "rotate-180" : ""],
								style: { "transition": "transform 0.2s" }
							}, null, 8, ["class"])
						], 8, ["onClick"]), createVNode(Transition, { name: "fade" }, {
							default: withCtx(() => [showFilter.value ? (openBlock(), createBlock("div", {
								key: 0,
								class: "absolute left-0 sm:left-auto sm:right-0 top-full mt-1 z-30 w-64 bg-card border border-border rounded-lg shadow-xl overflow-hidden"
							}, [
								createVNode("div", { class: "flex items-center justify-between px-3 py-2.5 border-b border-border" }, [createVNode("span", { class: "text-xs font-semibold text-foreground uppercase tracking-wide" }, "Filter"), activeFilterCount.value > 0 ? (openBlock(), createBlock("button", {
									key: 0,
									onClick: clearFilters,
									class: "text-xs text-red-500 hover:text-red-600 font-medium transition-colors"
								}, "Clear all")) : createCommentVNode("", true)]),
								createVNode("div", { class: "px-3 pt-3 pb-2" }, [createVNode("p", { class: "text-[11px] font-semibold text-muted-foreground uppercase tracking-wider mb-2" }, "Urutkan"), createVNode("div", { class: "space-y-1" }, [(openBlock(), createBlock(Fragment, null, renderList([
									{
										val: "terbaru",
										label: "Terbaru"
									},
									{
										val: "harga-termahal",
										label: "Harga Tertinggi"
									},
									{
										val: "harga-termurah",
										label: "Harga Terendah"
									}
								], (sortOption) => {
									return createVNode("label", {
										key: sortOption.val,
										class: "flex items-center gap-2.5 px-2 py-1.5 rounded-md hover:bg-muted/50 cursor-pointer transition-colors"
									}, [createVNode("div", {
										class: ["relative h-4 w-4 shrink-0 rounded border-2 flex items-center justify-center transition-all", sortBy.value === sortOption.val ? "bg-primary border-primary" : "border-border bg-background"],
										onClick: ($event) => sortBy.value = sortOption.val
									}, [sortBy.value === sortOption.val ? (openBlock(), createBlock("svg", {
										key: 0,
										xmlns: "http://www.w3.org/2000/svg",
										class: "h-2.5 w-2.5 text-primary-foreground",
										viewBox: "0 0 24 24",
										fill: "none",
										stroke: "currentColor",
										"stroke-width": "3.5"
									}, [createVNode("polyline", { points: "20 6 9 17 4 12" })])) : createCommentVNode("", true)], 10, ["onClick"]), createVNode("span", {
										class: "text-sm text-foreground select-none",
										onClick: ($event) => sortBy.value = sortOption.val
									}, toDisplayString(sortOption.label), 9, ["onClick"])]);
								}), 64))])]),
								createVNode("div", { class: "mx-3 border-t border-border" }),
								createVNode("div", { class: "px-3 pt-2 pb-2" }, [createVNode("p", { class: "text-[11px] font-semibold text-muted-foreground uppercase tracking-wider mb-2" }, "Status"), createVNode("div", { class: "space-y-1" }, [(openBlock(), createBlock(Fragment, null, renderList([
									{
										val: "all",
										label: "Semua Status"
									},
									{
										val: "aktif",
										label: "Aktif"
									},
									{
										val: "nonaktif",
										label: "Nonaktif"
									}
								], (statusOption) => {
									return createVNode("label", {
										key: statusOption.val,
										class: "flex items-center gap-2.5 px-2 py-1.5 rounded-md hover:bg-muted/50 cursor-pointer transition-colors"
									}, [createVNode("div", {
										class: ["relative h-4 w-4 shrink-0 rounded border-2 flex items-center justify-center transition-all", filterStatus.value === statusOption.val ? "bg-primary border-primary" : "border-border bg-background"],
										onClick: ($event) => filterStatus.value = statusOption.val
									}, [filterStatus.value === statusOption.val ? (openBlock(), createBlock("svg", {
										key: 0,
										xmlns: "http://www.w3.org/2000/svg",
										class: "h-2.5 w-2.5 text-primary-foreground",
										viewBox: "0 0 24 24",
										fill: "none",
										stroke: "currentColor",
										"stroke-width": "3.5"
									}, [createVNode("polyline", { points: "20 6 9 17 4 12" })])) : createCommentVNode("", true)], 10, ["onClick"]), createVNode("span", {
										class: "text-sm text-foreground select-none",
										onClick: ($event) => filterStatus.value = statusOption.val
									}, toDisplayString(statusOption.label), 9, ["onClick"])]);
								}), 64))])]),
								createVNode("div", { class: "mx-3 border-t border-border" }),
								createVNode("div", { class: "px-3 pt-2 pb-3" }, [createVNode("p", { class: "text-[11px] font-semibold text-muted-foreground uppercase tracking-wider mb-2" }, "Stok"), createVNode("div", { class: "space-y-1" }, [(openBlock(), createBlock(Fragment, null, renderList([
									{
										val: "all",
										label: "Semua Stok"
									},
									{
										val: "dilacak",
										label: "Dilacak"
									},
									{
										val: "bebas",
										label: "Bebas"
									}
								], (stockOption) => {
									return createVNode("label", {
										key: stockOption.val,
										class: "flex items-center gap-2.5 px-2 py-1.5 rounded-md hover:bg-muted/50 cursor-pointer transition-colors"
									}, [createVNode("div", {
										class: ["relative h-4 w-4 shrink-0 rounded border-2 flex items-center justify-center transition-all", filterStock.value === stockOption.val ? "bg-primary border-primary" : "border-border bg-background"],
										onClick: ($event) => filterStock.value = stockOption.val
									}, [filterStock.value === stockOption.val ? (openBlock(), createBlock("svg", {
										key: 0,
										xmlns: "http://www.w3.org/2000/svg",
										class: "h-2.5 w-2.5 text-primary-foreground",
										viewBox: "0 0 24 24",
										fill: "none",
										stroke: "currentColor",
										"stroke-width": "3.5"
									}, [createVNode("polyline", { points: "20 6 9 17 4 12" })])) : createCommentVNode("", true)], 10, ["onClick"]), createVNode("span", {
										class: "text-sm text-foreground select-none",
										onClick: ($event) => filterStock.value = stockOption.val
									}, toDisplayString(stockOption.label), 9, ["onClick"])]);
								}), 64))])])
							])) : createCommentVNode("", true)]),
							_: 1
						})], 512), unref(can)("produk.store") ? (openBlock(), createBlock(_sfc_main$3, {
							key: 0,
							onClick: openCreate,
							size: "sm",
							class: "flex-1 sm:flex-none flex items-center justify-center gap-2 bg-primary hover:bg-primary/90 text-primary-foreground"
						}, {
							default: withCtx(() => [createVNode(unref(Plus), { class: "h-4 w-4" }), createVNode("span", null, "Tambah Produk")]),
							_: 1
						})) : createCommentVNode("", true)])])]),
						error.value ? (openBlock(), createBlock(_sfc_main$7, {
							key: 0,
							variant: "destructive",
							class: "mb-4"
						}, {
							default: withCtx(() => [createTextVNode(toDisplayString(error.value), 1)]),
							_: 1
						})) : createCommentVNode("", true),
						createVNode(_sfc_main$4, { class: "border-zinc-200 dark:border-zinc-800 shadow-sm overflow-hidden" }, {
							default: withCtx(() => [createVNode(_sfc_main$5, { class: "p-0" }, {
								default: withCtx(() => [loading.value ? (openBlock(), createBlock("div", {
									key: 0,
									class: "flex flex-col items-center justify-center py-24 gap-3"
								}, [createVNode(unref(Loader2), { class: "h-7 w-7 animate-spin text-primary/50" }), createVNode("p", { class: "text-xs text-muted-foreground" }, "Memuat data...")])) : filteredProducts.value.length === 0 ? (openBlock(), createBlock("div", {
									key: 1,
									class: "flex flex-col items-center justify-center py-24 text-muted-foreground"
								}, [
									createVNode("div", { class: "w-14 h-14 rounded-full bg-muted flex items-center justify-center mb-4" }, [createVNode(unref(Package), { class: "h-7 w-7 opacity-40" })]),
									createVNode("p", { class: "text-sm font-medium" }, "Belum ada produk"),
									createVNode("p", { class: "text-xs text-muted-foreground/70 mt-1" }, "Mulai dengan menambahkan produk pertama Anda."),
									unref(can)("produk.store") && !searchQuery.value ? (openBlock(), createBlock(_sfc_main$3, {
										key: 0,
										size: "sm",
										class: "mt-4",
										onClick: openCreate
									}, {
										default: withCtx(() => [createVNode(unref(Plus), { class: "h-3.5 w-3.5 mr-1.5" }), createTextVNode(" Tambah Produk ")]),
										_: 1
									})) : createCommentVNode("", true)
								])) : (openBlock(), createBlock("div", { key: 2 }, [createVNode("div", { class: "md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60" }, [(openBlock(true), createBlock(Fragment, null, renderList(filteredProducts.value, (product) => {
									return openBlock(), createBlock("div", {
										key: "mobile-" + product.id,
										class: "p-4 flex flex-col gap-3 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors"
									}, [createVNode("div", { class: "flex items-start justify-between gap-3" }, [createVNode("div", { class: "flex items-center gap-3" }, [product.imageUrl ? (openBlock(), createBlock("img", {
										key: 0,
										src: product.imageUrl,
										class: "w-12 h-12 rounded-lg object-cover border border-zinc-200 dark:border-zinc-800"
									}, null, 8, ["src"])) : (openBlock(), createBlock("div", {
										key: 1,
										class: "w-12 h-12 rounded-lg flex items-center justify-center text-sm font-bold shrink-0 select-none border border-zinc-200 dark:border-zinc-800/50",
										style: productAvatarStyle(product.name)
									}, toDisplayString(product.name?.charAt(0).toUpperCase()), 5)), createVNode("div", null, [createVNode("h4", { class: "font-medium text-sm text-zinc-900 dark:text-zinc-100 line-clamp-1" }, toDisplayString(product.name), 1), product.sku ? (openBlock(), createBlock("span", {
										key: 0,
										class: "font-mono text-[10px] bg-zinc-100 dark:bg-zinc-800 text-zinc-500 px-1.5 py-0.5 rounded mt-1 inline-block"
									}, toDisplayString(product.sku), 1)) : createCommentVNode("", true)])]), createVNode("div", { class: "flex items-center gap-1 shrink-0" }, [unref(can)("produk.update") ? (openBlock(), createBlock(_sfc_main$3, {
										key: 0,
										variant: "ghost",
										size: "icon",
										class: "h-8 w-8 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 bg-zinc-50 dark:bg-zinc-800/50",
										onClick: ($event) => openEdit(product)
									}, {
										default: withCtx(() => [createVNode(unref(Pencil), { class: "h-3.5 w-3.5" })]),
										_: 1
									}, 8, ["onClick"])) : createCommentVNode("", true), unref(can)("produk.delete") ? (openBlock(), createBlock(_sfc_main$3, {
										key: 1,
										variant: "ghost",
										size: "icon",
										class: "h-8 w-8 text-zinc-400 hover:text-destructive bg-zinc-50 dark:bg-zinc-800/50",
										onClick: ($event) => doDelete(product)
									}, {
										default: withCtx(() => [createVNode(unref(Trash2), { class: "h-3.5 w-3.5" })]),
										_: 1
									}, 8, ["onClick"])) : createCommentVNode("", true)])]), createVNode("div", { class: "flex items-center justify-between mt-1" }, [createVNode("span", { class: "text-sm font-semibold text-zinc-800 dark:text-zinc-200" }, toDisplayString(formatCurrency(product.price)), 1), createVNode("div", { class: "flex items-center gap-2" }, [product.categoryName ? (openBlock(), createBlock("span", {
										key: 0,
										class: "text-[10px] font-medium px-2 py-0.5 rounded-full bg-blue-50 text-blue-700 dark:bg-blue-900/20 dark:text-blue-300 border border-blue-100 dark:border-blue-800/40"
									}, toDisplayString(product.categoryName), 1)) : createCommentVNode("", true), createVNode("span", { class: ["inline-flex items-center gap-1 text-[10px] font-medium px-2 py-0.5 rounded-full", product.isActive ? "bg-primary/10 text-primary border border-primary/20" : "bg-zinc-100 dark:bg-zinc-800 text-zinc-500 border border-zinc-200 dark:border-zinc-700"] }, [createVNode("span", { class: ["w-1 h-1 rounded-full", product.isActive ? "bg-primary" : "bg-zinc-400"] }, null, 2), createTextVNode(" " + toDisplayString(product.isActive ? "Aktif" : "Nonaktif"), 1)], 2)])])]);
								}), 128))]), createVNode("div", { class: "hidden md:block overflow-x-auto" }, [createVNode(_sfc_main$11, null, {
									default: withCtx(() => [createVNode(_sfc_main$12, null, {
										default: withCtx(() => [createVNode(_sfc_main$13, { class: "hover:bg-transparent border-b border-zinc-100 dark:border-zinc-800" }, {
											default: withCtx(() => [
												createVNode(_sfc_main$14, { class: "pl-5 py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500 w-[260px]" }, {
													default: withCtx(() => [createTextVNode("Produk")]),
													_: 1
												}),
												createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
													default: withCtx(() => [createTextVNode("SKU")]),
													_: 1
												}),
												createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
													default: withCtx(() => [createTextVNode("Harga")]),
													_: 1
												}),
												createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
													default: withCtx(() => [createTextVNode("Kategori")]),
													_: 1
												}),
												createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500 text-center" }, {
													default: withCtx(() => [createTextVNode("Stok")]),
													_: 1
												}),
												createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500 text-center" }, {
													default: withCtx(() => [createTextVNode("Status")]),
													_: 1
												}),
												createVNode(_sfc_main$14, { class: "py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500" }, {
													default: withCtx(() => [createTextVNode("Dibuat")]),
													_: 1
												}),
												createVNode(_sfc_main$14, { class: "pr-5 py-3 text-right" })
											]),
											_: 1
										})]),
										_: 1
									}), createVNode(_sfc_main$15, null, {
										default: withCtx(() => [(openBlock(true), createBlock(Fragment, null, renderList(filteredProducts.value, (product) => {
											return openBlock(), createBlock(_sfc_main$13, {
												key: product.id,
												class: "group border-b border-zinc-100 dark:border-zinc-800/60 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors"
											}, {
												default: withCtx(() => [
													createVNode(_sfc_main$16, { class: "pl-5 py-3" }, {
														default: withCtx(() => [createVNode("div", { class: "flex items-center gap-3" }, [product.imageUrl ? (openBlock(), createBlock("img", {
															key: 0,
															src: product.imageUrl,
															class: "w-8 h-8 rounded-lg object-cover border border-zinc-200 dark:border-zinc-700"
														}, null, 8, ["src"])) : (openBlock(), createBlock("div", {
															key: 1,
															class: "w-8 h-8 rounded-lg flex items-center justify-center text-xs font-bold shrink-0 select-none border border-zinc-200 dark:border-zinc-700/50",
															style: productAvatarStyle(product.name)
														}, toDisplayString(product.name?.charAt(0).toUpperCase()), 5)), createVNode("span", { class: "font-medium text-sm text-zinc-900 dark:text-zinc-100 truncate max-w-[170px]" }, toDisplayString(product.name), 1)])]),
														_: 2
													}, 1024),
													createVNode(_sfc_main$16, { class: "py-3" }, {
														default: withCtx(() => [product.sku ? (openBlock(), createBlock("span", {
															key: 0,
															class: "font-mono text-xs bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-400 px-2 py-1 rounded"
														}, toDisplayString(product.sku), 1)) : (openBlock(), createBlock("span", {
															key: 1,
															class: "text-zinc-300 dark:text-zinc-600 text-sm"
														}, "—"))]),
														_: 2
													}, 1024),
													createVNode(_sfc_main$16, { class: "py-3" }, {
														default: withCtx(() => [createVNode("span", { class: "text-sm font-semibold text-zinc-800 dark:text-zinc-200" }, toDisplayString(formatCurrency(product.price)), 1)]),
														_: 2
													}, 1024),
													createVNode(_sfc_main$16, { class: "py-3" }, {
														default: withCtx(() => [product.categoryName ? (openBlock(), createBlock("span", {
															key: 0,
															class: "inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full bg-blue-50 dark:bg-blue-900/20 text-blue-700 dark:text-blue-300 border border-blue-100 dark:border-blue-800/40"
														}, toDisplayString(product.categoryName), 1)) : (openBlock(), createBlock("span", {
															key: 1,
															class: "text-zinc-300 dark:text-zinc-600 text-sm"
														}, "—"))]),
														_: 2
													}, 1024),
													createVNode(_sfc_main$16, { class: "py-3 text-center" }, {
														default: withCtx(() => [createVNode("span", { class: ["inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full", product.trackStock ? "bg-emerald-50 dark:bg-emerald-900/20 text-emerald-700 dark:text-emerald-400 border border-emerald-100 dark:border-emerald-800/40" : "bg-zinc-100 dark:bg-zinc-800 text-zinc-500 border border-zinc-200 dark:border-zinc-700"] }, [createVNode("span", { class: ["w-1.5 h-1.5 rounded-full", product.trackStock ? "bg-emerald-500" : "bg-zinc-400"] }, null, 2), createTextVNode(" " + toDisplayString(product.trackStock ? "Dilacak" : "Bebas"), 1)], 2)]),
														_: 2
													}, 1024),
													createVNode(_sfc_main$16, { class: "py-3 text-center" }, {
														default: withCtx(() => [createVNode("span", { class: ["inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full", product.isActive ? "bg-primary/10 text-primary border border-primary/20" : "bg-zinc-100 dark:bg-zinc-800 text-zinc-500 border border-zinc-200 dark:border-zinc-700"] }, [createVNode("span", { class: ["w-1.5 h-1.5 rounded-full", product.isActive ? "bg-primary" : "bg-zinc-400"] }, null, 2), createTextVNode(" " + toDisplayString(product.isActive ? "Aktif" : "Nonaktif"), 1)], 2)]),
														_: 2
													}, 1024),
													createVNode(_sfc_main$16, { class: "py-3" }, {
														default: withCtx(() => [createVNode("span", { class: "text-xs text-zinc-400 dark:text-zinc-500" }, toDisplayString(formatDate(product.createdAt)), 1)]),
														_: 2
													}, 1024),
													createVNode(_sfc_main$16, { class: "pr-4 py-3 text-right" }, {
														default: withCtx(() => [createVNode("div", { class: "flex justify-end gap-1 transition-opacity" }, [unref(can)("produk.update") ? (openBlock(), createBlock(_sfc_main$3, {
															key: 0,
															variant: "ghost",
															size: "icon",
															class: "h-7 w-7 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 hover:bg-zinc-100 dark:hover:bg-zinc-800 rounded-md",
															title: "Edit",
															onClick: ($event) => openEdit(product)
														}, {
															default: withCtx(() => [createVNode(unref(Pencil), { class: "h-3.5 w-3.5" })]),
															_: 1
														}, 8, ["onClick"])) : createCommentVNode("", true), unref(can)("produk.delete") ? (openBlock(), createBlock(_sfc_main$3, {
															key: 1,
															variant: "ghost",
															size: "icon",
															class: "h-7 w-7 text-zinc-400 hover:text-destructive hover:bg-destructive/10 rounded-md",
															title: "Hapus",
															onClick: ($event) => doDelete(product)
														}, {
															default: withCtx(() => [createVNode(unref(Trash2), { class: "h-3.5 w-3.5" })]),
															_: 1
														}, 8, ["onClick"])) : createCommentVNode("", true)])]),
														_: 2
													}, 1024)
												]),
												_: 2
											}, 1024);
										}), 128))]),
										_: 1
									})]),
									_: 1
								})])])), pagination.value.totalElements > 0 && !loading.value ? (openBlock(), createBlock(_sfc_main$10, {
									key: 3,
									page: pagination.value.page + 1,
									"page-size": pagination.value.size,
									total: pagination.value.totalElements,
									"onUpdate:page": ($event) => fetchProducts($event - 1),
									"onUpdate:pageSize": ($event) => updatePageSize($event)
								}, null, 8, [
									"page",
									"page-size",
									"total",
									"onUpdate:page",
									"onUpdate:pageSize"
								])) : createCommentVNode("", true)]),
								_: 1
							})]),
							_: 1
						})
					]), (openBlock(), createBlock(Teleport, { to: "body" }, [
						createVNode(Transition, { name: "fade" }, {
							default: withCtx(() => [showDrawer.value ? (openBlock(), createBlock("div", {
								key: 0,
								class: "fixed inset-0 z-[50] bg-black/40 backdrop-blur-sm",
								onClick: closeDrawer
							})) : createCommentVNode("", true)]),
							_: 1
						}),
						createVNode(Transition, { name: "slide-right" }, {
							default: withCtx(() => [showDrawer.value ? (openBlock(), createBlock("div", {
								key: 0,
								class: "fixed inset-y-0 right-0 z-[50] flex flex-col w-full sm:max-w-[420px] h-full bg-card shadow-2xl sm:border-l overflow-hidden"
							}, [
								createVNode("div", { class: "flex items-center justify-between px-6 py-4 border-b shrink-0" }, [createVNode("div", null, [createVNode("h3", { class: "font-semibold text-base" }, toDisplayString(modalMode.value === "create" ? "Tambah Produk" : "Edit Produk"), 1), createVNode("p", { class: "text-xs text-muted-foreground mt-0.5" }, toDisplayString(modalMode.value === "create" ? "Isi detail produk baru." : "Perbarui informasi produk."), 1)]), createVNode(_sfc_main$3, {
									variant: "ghost",
									size: "icon",
									onClick: closeDrawer
								}, {
									default: withCtx(() => [createVNode(unref(X), { class: "h-4 w-4" })]),
									_: 1
								})]),
								createVNode("div", { class: "flex-1 overflow-y-auto px-6 py-5 space-y-5" }, [
									formError.value ? (openBlock(), createBlock(_sfc_main$7, {
										key: 0,
										variant: "destructive"
									}, {
										default: withCtx(() => [createVNode("p", { class: "text-sm" }, toDisplayString(formError.value), 1)]),
										_: 1
									})) : createCommentVNode("", true),
									createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$8, null, {
										default: withCtx(() => [createTextVNode("Foto Produk")]),
										_: 1
									}), createVNode("div", { class: "flex items-center gap-4" }, [createVNode("div", { class: "h-20 w-20 shrink-0 overflow-hidden rounded-lg border border-dashed border-zinc-300 dark:border-zinc-700 bg-zinc-50 dark:bg-zinc-900 flex items-center justify-center relative" }, [form.value.imagePreview ? (openBlock(), createBlock("img", {
										key: 0,
										src: form.value.imagePreview,
										alt: "Preview",
										class: "h-full w-full object-cover"
									}, null, 8, ["src"])) : (openBlock(), createBlock(unref(Package), {
										key: 1,
										class: "h-8 w-8 text-zinc-400"
									})), createVNode("input", {
										type: "file",
										accept: "image/*",
										class: "absolute inset-0 w-full h-full opacity-0 cursor-pointer",
										onChange: handleImageUpload,
										disabled: saving.value
									}, null, 40, ["disabled"])]), createVNode("div", { class: "flex flex-col gap-2" }, [createVNode("div", { class: "relative" }, [createVNode(_sfc_main$3, {
										type: "button",
										variant: "outline",
										size: "sm"
									}, {
										default: withCtx(() => [createVNode(unref(Upload), { class: "h-3.5 w-3.5 mr-2" }), createTextVNode(" Pilih Foto ")]),
										_: 1
									}), createVNode("input", {
										type: "file",
										accept: "image/*",
										class: "absolute inset-0 w-full h-full opacity-0 cursor-pointer",
										onChange: handleImageUpload,
										disabled: saving.value
									}, null, 40, ["disabled"])]), createVNode("p", { class: "text-[10px] text-muted-foreground" }, "Format: JPG, PNG. Maks 2MB.")])])]),
									createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$8, { for: "f-name" }, {
										default: withCtx(() => [createTextVNode("Nama Produk "), createVNode("span", { class: "text-destructive" }, "*")]),
										_: 1
									}), createVNode(_sfc_main$6, {
										id: "f-name",
										modelValue: form.value.name,
										"onUpdate:modelValue": ($event) => form.value.name = $event,
										placeholder: "Contoh: Kaos Polos Putih",
										disabled: saving.value
									}, null, 8, [
										"modelValue",
										"onUpdate:modelValue",
										"disabled"
									])]),
									createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$8, { for: "f-sku" }, {
										default: withCtx(() => [createTextVNode("SKU "), createVNode("span", { class: "text-muted-foreground text-xs" }, "(opsional)")]),
										_: 1
									}), createVNode(_sfc_main$6, {
										id: "f-sku",
										modelValue: form.value.sku,
										"onUpdate:modelValue": ($event) => form.value.sku = $event,
										placeholder: "Contoh: KPP-001",
										disabled: saving.value
									}, null, 8, [
										"modelValue",
										"onUpdate:modelValue",
										"disabled"
									])]),
									createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$8, { for: "f-price" }, {
										default: withCtx(() => [createTextVNode("Harga "), createVNode("span", { class: "text-destructive" }, "*")]),
										_: 1
									}), createVNode("div", { class: "relative" }, [createVNode("span", { class: "absolute left-3 top-1/2 -translate-y-1/2 text-sm text-muted-foreground" }, "Rp"), createVNode(_sfc_main$6, {
										id: "f-price",
										modelValue: form.value.price,
										"onUpdate:modelValue": ($event) => form.value.price = $event,
										type: "number",
										min: "0",
										step: "100",
										placeholder: "0",
										disabled: saving.value,
										class: "pl-9"
									}, null, 8, [
										"modelValue",
										"onUpdate:modelValue",
										"disabled"
									])])]),
									createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$8, { for: "f-category" }, {
										default: withCtx(() => [createTextVNode("Kategori "), createVNode("span", { class: "text-muted-foreground text-xs" }, "(opsional)")]),
										_: 1
									}), withDirectives(createVNode("select", {
										id: "f-category",
										"onUpdate:modelValue": ($event) => form.value.categoryId = $event,
										disabled: saving.value,
										class: "flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50"
									}, [createVNode("option", { value: null }, "— Pilih kategori —"), (openBlock(true), createBlock(Fragment, null, renderList(categories.value, (cat) => {
										return openBlock(), createBlock("option", {
											key: cat.id,
											value: cat.id
										}, toDisplayString(cat.name), 9, ["value"]);
									}), 128))], 8, ["onUpdate:modelValue", "disabled"]), [[vModelSelect, form.value.categoryId]])]),
									createVNode("div", { class: "flex items-center justify-between rounded-lg border p-4" }, [createVNode("div", { class: "space-y-0.5" }, [createVNode(_sfc_main$8, {
										class: "text-sm font-medium cursor-pointer",
										for: "f-track-stock"
									}, {
										default: withCtx(() => [createTextVNode("Lacak Stok")]),
										_: 1
									}), createVNode("p", { class: "text-xs text-muted-foreground" }, "Nonaktifkan untuk produk layanan.")]), createVNode("button", {
										id: "f-track-stock",
										type: "button",
										role: "switch",
										"aria-checked": form.value.trackStock,
										disabled: saving.value,
										class: ["relative inline-flex h-6 w-11 shrink-0 cursor-pointer items-center rounded-full border-2 border-transparent transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50", form.value.trackStock ? "bg-primary" : "bg-input"],
										onClick: ($event) => form.value.trackStock = !form.value.trackStock
									}, [createVNode("span", { class: ["pointer-events-none block h-5 w-5 rounded-full bg-background shadow-lg ring-0 transition-transform", form.value.trackStock ? "translate-x-5" : "translate-x-0"] }, null, 2)], 10, [
										"aria-checked",
										"disabled",
										"onClick"
									])]),
									createVNode("div", { class: "flex items-center justify-between rounded-lg border p-4" }, [createVNode("div", { class: "space-y-0.5" }, [createVNode(_sfc_main$8, {
										class: "text-sm font-medium cursor-pointer",
										for: "f-is-active"
									}, {
										default: withCtx(() => [createTextVNode("Produk Aktif")]),
										_: 1
									}), createVNode("p", { class: "text-xs text-muted-foreground" }, "Produk nonaktif disembunyikan.")]), createVNode("button", {
										id: "f-is-active",
										type: "button",
										role: "switch",
										"aria-checked": form.value.isActive,
										disabled: saving.value,
										class: ["relative inline-flex h-6 w-11 shrink-0 cursor-pointer items-center rounded-full border-2 border-transparent transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50", form.value.isActive ? "bg-primary" : "bg-input"],
										onClick: ($event) => form.value.isActive = !form.value.isActive
									}, [createVNode("span", { class: ["pointer-events-none block h-5 w-5 rounded-full bg-background shadow-lg ring-0 transition-transform", form.value.isActive ? "translate-x-5" : "translate-x-0"] }, null, 2)], 10, [
										"aria-checked",
										"disabled",
										"onClick"
									])])
								]),
								createVNode("div", { class: "flex justify-end gap-3 px-6 py-4 border-t shrink-0 bg-muted/30" }, [createVNode(_sfc_main$3, {
									variant: "outline",
									onClick: closeDrawer,
									disabled: saving.value
								}, {
									default: withCtx(() => [createTextVNode("Batal")]),
									_: 1
								}, 8, ["disabled"]), createVNode(_sfc_main$3, {
									onClick: saveProduct,
									disabled: saving.value
								}, {
									default: withCtx(() => [saving.value ? (openBlock(), createBlock(unref(Loader2), {
										key: 0,
										class: "h-4 w-4 mr-2 animate-spin"
									})) : createCommentVNode("", true), createTextVNode(" " + toDisplayString(modalMode.value === "create" ? "Simpan Produk" : "Perbarui"), 1)]),
									_: 1
								}, 8, ["disabled"])])
							])) : createCommentVNode("", true)]),
							_: 1
						}),
						createVNode(Transition, { name: "fade" }, {
							default: withCtx(() => [deleteModal.value.show ? (openBlock(), createBlock("div", {
								key: 0,
								class: "fixed inset-0 z-[60] bg-black/40 backdrop-blur-sm",
								onClick: closeDeleteModal
							})) : createCommentVNode("", true)]),
							_: 1
						}),
						createVNode(Transition, { name: "scale" }, {
							default: withCtx(() => [deleteModal.value.show ? (openBlock(), createBlock("div", {
								key: 0,
								class: "fixed inset-0 z-[60] flex items-center justify-center p-4 pointer-events-none"
							}, [createVNode("div", { class: "relative bg-card rounded-xl shadow-2xl w-full max-w-md overflow-hidden border border-border pointer-events-auto" }, [createVNode("div", { class: "p-6" }, [
								createVNode("h3", { class: "text-lg font-semibold text-destructive flex items-center gap-2" }, [createVNode(unref(Trash2), { class: "h-5 w-5" }), createTextVNode(" Hapus Produk ")]),
								createVNode("p", { class: "text-sm text-muted-foreground mt-2" }, [
									createTextVNode(" Tindakan ini tidak dapat dibatalkan. Hal ini akan menghapus produk "),
									createVNode("span", { class: "font-semibold text-foreground" }, toDisplayString(deleteModal.value.product?.name), 1),
									createTextVNode(" secara permanen. ")
								]),
								createVNode("div", { class: "mt-4" }, [createVNode(_sfc_main$8, { class: "text-sm font-medium" }, {
									default: withCtx(() => [
										createTextVNode(" Ketik "),
										createVNode("span", { class: "font-bold select-all bg-muted px-1.5 py-0.5 rounded text-foreground" }, toDisplayString(deleteModal.value.product?.name), 1),
										createTextVNode(" untuk mengonfirmasi. ")
									]),
									_: 1
								}), createVNode(_sfc_main$6, {
									modelValue: deleteModal.value.confirmText,
									"onUpdate:modelValue": ($event) => deleteModal.value.confirmText = $event,
									class: "mt-2",
									placeholder: "Masukkan nama produk"
								}, null, 8, ["modelValue", "onUpdate:modelValue"])])
							]), createVNode("div", { class: "flex items-center justify-end gap-3 px-6 py-4 bg-muted/30 border-t" }, [createVNode(_sfc_main$3, {
								variant: "outline",
								onClick: closeDeleteModal
							}, {
								default: withCtx(() => [createTextVNode("Batal")]),
								_: 1
							}), createVNode(_sfc_main$3, {
								variant: "destructive",
								disabled: deleteModal.value.confirmText !== deleteModal.value.product?.name || deleting.value,
								onClick: confirmDelete
							}, {
								default: withCtx(() => [deleting.value ? (openBlock(), createBlock(unref(Loader2), {
									key: 0,
									class: "h-4 w-4 mr-2 animate-spin"
								})) : createCommentVNode("", true), createTextVNode(" Hapus Sekarang ")]),
								_: 1
							}, 8, ["disabled"])])])])) : createCommentVNode("", true)]),
							_: 1
						})
					]))];
				}),
				_: 1
			}, _parent));
		};
	}
};
var _sfc_setup = _sfc_main.setup;
_sfc_main.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/pages/ProductPage.vue");
	return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
var ProductPage_default = /* @__PURE__ */ _plugin_vue_export_helper_default(_sfc_main, [["__scopeId", "data-v-11961a06"]]);
//#endregion
export { ProductPage_default as default };
