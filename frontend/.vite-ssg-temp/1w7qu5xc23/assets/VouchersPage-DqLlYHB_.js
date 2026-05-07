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
import { t as _sfc_main$10 } from "./Badge-PdtEYXOU.js";
import { Fragment, Teleport, Transition, computed, createBlock, createCommentVNode, createTextVNode, createVNode, onMounted, openBlock, ref, renderList, toDisplayString, unref, useSSRContext, vModelCheckbox, vModelSelect, withCtx, withDirectives, withKeys } from "vue";
import { ssrIncludeBooleanAttr, ssrInterpolate, ssrLooseContain, ssrLooseEqual, ssrRenderAttr, ssrRenderComponent, ssrRenderList, ssrRenderStyle, ssrRenderTeleport } from "vue/server-renderer";
import { Calendar, Loader2, Pencil, Plus, Ticket, Trash2, X } from "lucide-vue-next";
//#region src/pages/VouchersPage.vue
var _sfc_main = {
	__name: "VouchersPage",
	__ssrInlineRender: true,
	setup(__props) {
		const { can } = usePermission();
		const { toast } = useToast();
		const { confirm } = useConfirm();
		const vouchers = ref([]);
		const partners = ref([]);
		const loading = ref(false);
		const error = ref(null);
		const searchQuery = ref("");
		const page = ref(1);
		const pageSize = ref(10);
		const filteredVouchers = computed(() => {
			if (!searchQuery.value) return vouchers.value;
			const q = searchQuery.value.toLowerCase();
			return vouchers.value.filter((v) => v.name.toLowerCase().includes(q) || v.code?.toLowerCase().includes(q));
		});
		const paginatedVouchers = computed(() => {
			const start = (page.value - 1) * pageSize.value;
			return filteredVouchers.value.slice(start, start + pageSize.value);
		});
		const showDrawer = ref(false);
		const modalMode = ref("create");
		const saving = ref(false);
		const formError = ref(null);
		const emptyForm = () => ({
			id: null,
			partner_id: "",
			code: "",
			name: "",
			discount_type: "percent",
			discount_value: 0,
			min_purchase: 0,
			max_discount: null,
			quota: null,
			valid_from: "",
			valid_until: "",
			is_active: true
		});
		const form = ref(emptyForm());
		const deleteModal = ref({
			show: false,
			voucher: null,
			confirmText: ""
		});
		const deleting = ref(false);
		function doDelete(voucher) {
			deleteModal.value = {
				show: true,
				voucher,
				confirmText: ""
			};
		}
		function closeDeleteModal() {
			deleteModal.value.show = false;
			setTimeout(() => {
				deleteModal.value.voucher = null;
				deleteModal.value.confirmText = "";
			}, 300);
		}
		async function confirmDelete() {
			if (deleteModal.value.confirmText !== deleteModal.value.voucher?.code) return;
			deleting.value = true;
			try {
				await api.delete(`/api/v1/vouchers/${deleteModal.value.voucher.id}`);
				toast.success("Voucer berhasil dihapus!");
				fetchVouchers();
				closeDeleteModal();
			} catch (err) {
				toast.error(err.response?.data?.message || "Gagal menghapus voucer.");
			} finally {
				deleting.value = false;
			}
		}
		async function fetchVouchers() {
			loading.value = true;
			try {
				vouchers.value = (await api.get("/api/v1/vouchers")).data.data;
			} catch (err) {
				error.value = "Gagal memuat data voucer.";
			} finally {
				loading.value = false;
			}
		}
		async function fetchPartners() {
			try {
				partners.value = (await api.get("/api/v1/partners")).data.data;
			} catch (err) {}
		}
		function openCreate() {
			form.value = emptyForm();
			formError.value = null;
			modalMode.value = "create";
			showDrawer.value = true;
		}
		function openEdit(v) {
			form.value = { ...v };
			if (form.value.valid_from) form.value.valid_from = form.value.valid_from.split("T")[0];
			if (form.value.valid_until) form.value.valid_until = form.value.valid_until.split("T")[0];
			formError.value = null;
			modalMode.value = "edit";
			showDrawer.value = true;
		}
		async function saveVoucher() {
			saving.value = true;
			formError.value = null;
			try {
				if (modalMode.value === "create") {
					await api.post("/api/v1/vouchers", form.value);
					toast.success("Voucer berhasil ditambahkan!");
				} else {
					await api.put(`/api/v1/vouchers/${form.value.id}`, form.value);
					toast.success("Voucer berhasil diperbarui!");
				}
				showDrawer.value = false;
				fetchVouchers();
			} catch (err) {
				formError.value = err.response?.data?.message || "Gagal menyimpan voucer.";
			} finally {
				saving.value = false;
			}
		}
		function formatCurrency(val) {
			if (val === null || val === void 0) return "-";
			return new Intl.NumberFormat("id-ID", {
				style: "currency",
				currency: "IDR",
				minimumFractionDigits: 0
			}).format(val);
		}
		function getPartnerName(id) {
			return partners.value.find((p) => p.id === id)?.name || "Unknown Partner";
		}
		onMounted(() => {
			fetchVouchers();
			fetchPartners();
		});
		return (_ctx, _push, _parent, _attrs) => {
			_push(ssrRenderComponent(_sfc_main$2, _attrs, {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) {
						_push(`<div class="pb-6" data-v-0d2cdf65${_scopeId}><div class="mb-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4" data-v-0d2cdf65${_scopeId}><div data-v-0d2cdf65${_scopeId}><h1 class="text-xl font-bold tracking-tight text-zinc-900 dark:text-zinc-100" data-v-0d2cdf65${_scopeId}>Manajemen Voucer</h1><p class="text-xs text-zinc-500 mt-0.5" data-v-0d2cdf65${_scopeId}>Kelola kode promo dan diskon belanja.</p></div>`);
						_push(ssrRenderComponent(_sfc_main$1, {
							onClick: openCreate,
							class: "bg-primary hover:bg-primary/90 flex items-center gap-2"
						}, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) {
									_push(ssrRenderComponent(unref(Plus), { class: "h-4 w-4" }, null, _parent, _scopeId));
									_push(`<span data-v-0d2cdf65${_scopeId}>Tambah Voucer</span>`);
								} else return [createVNode(unref(Plus), { class: "h-4 w-4" }), createVNode("span", null, "Tambah Voucer")];
							}),
							_: 1
						}, _parent, _scopeId));
						_push(`</div><div class="flex flex-col sm:flex-row items-center justify-between gap-4 mb-5" data-v-0d2cdf65${_scopeId}>`);
						_push(ssrRenderComponent(_sfc_main$8, {
							modelValue: searchQuery.value,
							"onUpdate:modelValue": ($event) => searchQuery.value = $event,
							placeholder: "Cari kode atau nama voucer...",
							class: "w-full sm:max-w-sm"
						}, null, _parent, _scopeId));
						_push(`</div>`);
						_push(ssrRenderComponent(_sfc_main$3, null, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) _push(ssrRenderComponent(_sfc_main$4, { class: "p-0" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) {
											if (loading.value) {
												_push(`<div class="flex items-center justify-center py-20" data-v-0d2cdf65${_scopeId}>`);
												_push(ssrRenderComponent(unref(Loader2), { class: "h-6 w-6 animate-spin text-muted-foreground" }, null, _parent, _scopeId));
												_push(`</div>`);
											} else if (filteredVouchers.value.length === 0) {
												_push(`<div class="flex flex-col items-center justify-center py-20 text-muted-foreground" data-v-0d2cdf65${_scopeId}>`);
												_push(ssrRenderComponent(unref(Ticket), { class: "h-10 w-10 mb-3 opacity-20" }, null, _parent, _scopeId));
												_push(`<p class="text-sm" data-v-0d2cdf65${_scopeId}>Belum ada data voucer.</p></div>`);
											} else {
												_push(`<div class="overflow-x-auto" data-v-0d2cdf65${_scopeId}><table class="w-full text-sm" data-v-0d2cdf65${_scopeId}><thead data-v-0d2cdf65${_scopeId}><tr class="bg-muted/40 border-b" data-v-0d2cdf65${_scopeId}><th class="px-4 py-3 text-left font-medium text-muted-foreground" data-v-0d2cdf65${_scopeId}>Voucer</th><th class="px-4 py-3 text-left font-medium text-muted-foreground" data-v-0d2cdf65${_scopeId}>Diskon</th><th class="px-4 py-3 text-left font-medium text-muted-foreground" data-v-0d2cdf65${_scopeId}>Kuota / Terpakai</th><th class="px-4 py-3 text-left font-medium text-muted-foreground" data-v-0d2cdf65${_scopeId}>Masa Berlaku</th><th class="px-4 py-3 text-left font-medium text-muted-foreground" data-v-0d2cdf65${_scopeId}>Status</th><th class="px-4 py-3 text-right font-medium text-muted-foreground" data-v-0d2cdf65${_scopeId}>Aksi</th></tr></thead><tbody data-v-0d2cdf65${_scopeId}><!--[-->`);
												ssrRenderList(paginatedVouchers.value, (v) => {
													_push(`<tr class="border-b last:border-0 hover:bg-muted/30 transition-colors" data-v-0d2cdf65${_scopeId}><td class="px-4 py-3" data-v-0d2cdf65${_scopeId}><div class="flex flex-col" data-v-0d2cdf65${_scopeId}><span class="font-mono text-xs font-bold text-primary" data-v-0d2cdf65${_scopeId}>${ssrInterpolate(v.code)}</span><span class="font-medium" data-v-0d2cdf65${_scopeId}>${ssrInterpolate(v.name)}</span><span class="text-[10px] text-muted-foreground" data-v-0d2cdf65${_scopeId}>${ssrInterpolate(getPartnerName(v.partner_id))}</span></div></td><td class="px-4 py-3" data-v-0d2cdf65${_scopeId}><div class="flex flex-col" data-v-0d2cdf65${_scopeId}><span class="font-semibold" data-v-0d2cdf65${_scopeId}>${ssrInterpolate(v.discount_type === "percent" ? v.discount_value + "%" : formatCurrency(v.discount_value))}</span>`);
													if (v.min_purchase > 0) _push(`<span class="text-[10px] text-muted-foreground" data-v-0d2cdf65${_scopeId}> Min. ${ssrInterpolate(formatCurrency(v.min_purchase))}</span>`);
													else _push(`<!---->`);
													_push(`</div></td><td class="px-4 py-3" data-v-0d2cdf65${_scopeId}><div class="flex flex-col" data-v-0d2cdf65${_scopeId}><span data-v-0d2cdf65${_scopeId}>${ssrInterpolate(v.quota || "∞")} / ${ssrInterpolate(v.used_count || 0)}</span><div class="w-24 h-1.5 bg-muted rounded-full mt-1 overflow-hidden" data-v-0d2cdf65${_scopeId}><div class="h-full bg-primary" style="${ssrRenderStyle({ width: v.quota ? Math.min(v.used_count / v.quota * 100, 100) + "%" : "0%" })}" data-v-0d2cdf65${_scopeId}></div></div></div></td><td class="px-4 py-3 text-xs" data-v-0d2cdf65${_scopeId}><div class="flex flex-col gap-0.5" data-v-0d2cdf65${_scopeId}><div class="flex items-center gap-1" data-v-0d2cdf65${_scopeId}>`);
													_push(ssrRenderComponent(unref(Calendar), { class: "h-3 w-3 text-muted-foreground" }, null, _parent, _scopeId));
													_push(`<span data-v-0d2cdf65${_scopeId}>${ssrInterpolate(v.valid_from || "-")}</span></div><div class="flex items-center gap-1" data-v-0d2cdf65${_scopeId}>`);
													_push(ssrRenderComponent(unref(Calendar), { class: "h-3 w-3 text-muted-foreground" }, null, _parent, _scopeId));
													_push(`<span data-v-0d2cdf65${_scopeId}>${ssrInterpolate(v.valid_until || "-")}</span></div></div></td><td class="px-4 py-3" data-v-0d2cdf65${_scopeId}>`);
													_push(ssrRenderComponent(_sfc_main$10, { variant: v.is_active ? "default" : "secondary" }, {
														default: withCtx((_, _push, _parent, _scopeId) => {
															if (_push) _push(`${ssrInterpolate(v.is_active ? "Aktif" : "Nonaktif")}`);
															else return [createTextVNode(toDisplayString(v.is_active ? "Aktif" : "Nonaktif"), 1)];
														}),
														_: 2
													}, _parent, _scopeId));
													_push(`</td><td class="px-4 py-3 text-right" data-v-0d2cdf65${_scopeId}><div class="flex justify-end gap-2" data-v-0d2cdf65${_scopeId}>`);
													_push(ssrRenderComponent(_sfc_main$1, {
														variant: "ghost",
														size: "icon",
														onClick: ($event) => openEdit(v)
													}, {
														default: withCtx((_, _push, _parent, _scopeId) => {
															if (_push) _push(ssrRenderComponent(unref(Pencil), { class: "h-4 w-4" }, null, _parent, _scopeId));
															else return [createVNode(unref(Pencil), { class: "h-4 w-4" })];
														}),
														_: 2
													}, _parent, _scopeId));
													_push(ssrRenderComponent(_sfc_main$1, {
														variant: "ghost",
														size: "icon",
														class: "text-destructive",
														onClick: ($event) => doDelete(v)
													}, {
														default: withCtx((_, _push, _parent, _scopeId) => {
															if (_push) _push(ssrRenderComponent(unref(Trash2), { class: "h-4 w-4" }, null, _parent, _scopeId));
															else return [createVNode(unref(Trash2), { class: "h-4 w-4" })];
														}),
														_: 2
													}, _parent, _scopeId));
													_push(`</div></td></tr>`);
												});
												_push(`<!--]--></tbody></table></div>`);
											}
											if (filteredVouchers.value.length > 0 && !loading.value) _push(ssrRenderComponent(_sfc_main$9, {
												page: page.value,
												"page-size": pageSize.value,
												total: filteredVouchers.value.length,
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
										}, [createVNode(unref(Loader2), { class: "h-6 w-6 animate-spin text-muted-foreground" })])) : filteredVouchers.value.length === 0 ? (openBlock(), createBlock("div", {
											key: 1,
											class: "flex flex-col items-center justify-center py-20 text-muted-foreground"
										}, [createVNode(unref(Ticket), { class: "h-10 w-10 mb-3 opacity-20" }), createVNode("p", { class: "text-sm" }, "Belum ada data voucer.")])) : (openBlock(), createBlock("div", {
											key: 2,
											class: "overflow-x-auto"
										}, [createVNode("table", { class: "w-full text-sm" }, [createVNode("thead", null, [createVNode("tr", { class: "bg-muted/40 border-b" }, [
											createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Voucer"),
											createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Diskon"),
											createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Kuota / Terpakai"),
											createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Masa Berlaku"),
											createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Status"),
											createVNode("th", { class: "px-4 py-3 text-right font-medium text-muted-foreground" }, "Aksi")
										])]), createVNode("tbody", null, [(openBlock(true), createBlock(Fragment, null, renderList(paginatedVouchers.value, (v) => {
											return openBlock(), createBlock("tr", {
												key: v.id,
												class: "border-b last:border-0 hover:bg-muted/30 transition-colors"
											}, [
												createVNode("td", { class: "px-4 py-3" }, [createVNode("div", { class: "flex flex-col" }, [
													createVNode("span", { class: "font-mono text-xs font-bold text-primary" }, toDisplayString(v.code), 1),
													createVNode("span", { class: "font-medium" }, toDisplayString(v.name), 1),
													createVNode("span", { class: "text-[10px] text-muted-foreground" }, toDisplayString(getPartnerName(v.partner_id)), 1)
												])]),
												createVNode("td", { class: "px-4 py-3" }, [createVNode("div", { class: "flex flex-col" }, [createVNode("span", { class: "font-semibold" }, toDisplayString(v.discount_type === "percent" ? v.discount_value + "%" : formatCurrency(v.discount_value)), 1), v.min_purchase > 0 ? (openBlock(), createBlock("span", {
													key: 0,
													class: "text-[10px] text-muted-foreground"
												}, " Min. " + toDisplayString(formatCurrency(v.min_purchase)), 1)) : createCommentVNode("", true)])]),
												createVNode("td", { class: "px-4 py-3" }, [createVNode("div", { class: "flex flex-col" }, [createVNode("span", null, toDisplayString(v.quota || "∞") + " / " + toDisplayString(v.used_count || 0), 1), createVNode("div", { class: "w-24 h-1.5 bg-muted rounded-full mt-1 overflow-hidden" }, [createVNode("div", {
													class: "h-full bg-primary",
													style: { width: v.quota ? Math.min(v.used_count / v.quota * 100, 100) + "%" : "0%" }
												}, null, 4)])])]),
												createVNode("td", { class: "px-4 py-3 text-xs" }, [createVNode("div", { class: "flex flex-col gap-0.5" }, [createVNode("div", { class: "flex items-center gap-1" }, [createVNode(unref(Calendar), { class: "h-3 w-3 text-muted-foreground" }), createVNode("span", null, toDisplayString(v.valid_from || "-"), 1)]), createVNode("div", { class: "flex items-center gap-1" }, [createVNode(unref(Calendar), { class: "h-3 w-3 text-muted-foreground" }), createVNode("span", null, toDisplayString(v.valid_until || "-"), 1)])])]),
												createVNode("td", { class: "px-4 py-3" }, [createVNode(_sfc_main$10, { variant: v.is_active ? "default" : "secondary" }, {
													default: withCtx(() => [createTextVNode(toDisplayString(v.is_active ? "Aktif" : "Nonaktif"), 1)]),
													_: 2
												}, 1032, ["variant"])]),
												createVNode("td", { class: "px-4 py-3 text-right" }, [createVNode("div", { class: "flex justify-end gap-2" }, [createVNode(_sfc_main$1, {
													variant: "ghost",
													size: "icon",
													onClick: ($event) => openEdit(v)
												}, {
													default: withCtx(() => [createVNode(unref(Pencil), { class: "h-4 w-4" })]),
													_: 1
												}, 8, ["onClick"]), createVNode(_sfc_main$1, {
													variant: "ghost",
													size: "icon",
													class: "text-destructive",
													onClick: ($event) => doDelete(v)
												}, {
													default: withCtx(() => [createVNode(unref(Trash2), { class: "h-4 w-4" })]),
													_: 1
												}, 8, ["onClick"])])])
											]);
										}), 128))])])])), filteredVouchers.value.length > 0 && !loading.value ? (openBlock(), createBlock(_sfc_main$9, {
											key: 3,
											page: page.value,
											"page-size": pageSize.value,
											total: filteredVouchers.value.length,
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
									}, [createVNode(unref(Loader2), { class: "h-6 w-6 animate-spin text-muted-foreground" })])) : filteredVouchers.value.length === 0 ? (openBlock(), createBlock("div", {
										key: 1,
										class: "flex flex-col items-center justify-center py-20 text-muted-foreground"
									}, [createVNode(unref(Ticket), { class: "h-10 w-10 mb-3 opacity-20" }), createVNode("p", { class: "text-sm" }, "Belum ada data voucer.")])) : (openBlock(), createBlock("div", {
										key: 2,
										class: "overflow-x-auto"
									}, [createVNode("table", { class: "w-full text-sm" }, [createVNode("thead", null, [createVNode("tr", { class: "bg-muted/40 border-b" }, [
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Voucer"),
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Diskon"),
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Kuota / Terpakai"),
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Masa Berlaku"),
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Status"),
										createVNode("th", { class: "px-4 py-3 text-right font-medium text-muted-foreground" }, "Aksi")
									])]), createVNode("tbody", null, [(openBlock(true), createBlock(Fragment, null, renderList(paginatedVouchers.value, (v) => {
										return openBlock(), createBlock("tr", {
											key: v.id,
											class: "border-b last:border-0 hover:bg-muted/30 transition-colors"
										}, [
											createVNode("td", { class: "px-4 py-3" }, [createVNode("div", { class: "flex flex-col" }, [
												createVNode("span", { class: "font-mono text-xs font-bold text-primary" }, toDisplayString(v.code), 1),
												createVNode("span", { class: "font-medium" }, toDisplayString(v.name), 1),
												createVNode("span", { class: "text-[10px] text-muted-foreground" }, toDisplayString(getPartnerName(v.partner_id)), 1)
											])]),
											createVNode("td", { class: "px-4 py-3" }, [createVNode("div", { class: "flex flex-col" }, [createVNode("span", { class: "font-semibold" }, toDisplayString(v.discount_type === "percent" ? v.discount_value + "%" : formatCurrency(v.discount_value)), 1), v.min_purchase > 0 ? (openBlock(), createBlock("span", {
												key: 0,
												class: "text-[10px] text-muted-foreground"
											}, " Min. " + toDisplayString(formatCurrency(v.min_purchase)), 1)) : createCommentVNode("", true)])]),
											createVNode("td", { class: "px-4 py-3" }, [createVNode("div", { class: "flex flex-col" }, [createVNode("span", null, toDisplayString(v.quota || "∞") + " / " + toDisplayString(v.used_count || 0), 1), createVNode("div", { class: "w-24 h-1.5 bg-muted rounded-full mt-1 overflow-hidden" }, [createVNode("div", {
												class: "h-full bg-primary",
												style: { width: v.quota ? Math.min(v.used_count / v.quota * 100, 100) + "%" : "0%" }
											}, null, 4)])])]),
											createVNode("td", { class: "px-4 py-3 text-xs" }, [createVNode("div", { class: "flex flex-col gap-0.5" }, [createVNode("div", { class: "flex items-center gap-1" }, [createVNode(unref(Calendar), { class: "h-3 w-3 text-muted-foreground" }), createVNode("span", null, toDisplayString(v.valid_from || "-"), 1)]), createVNode("div", { class: "flex items-center gap-1" }, [createVNode(unref(Calendar), { class: "h-3 w-3 text-muted-foreground" }), createVNode("span", null, toDisplayString(v.valid_until || "-"), 1)])])]),
											createVNode("td", { class: "px-4 py-3" }, [createVNode(_sfc_main$10, { variant: v.is_active ? "default" : "secondary" }, {
												default: withCtx(() => [createTextVNode(toDisplayString(v.is_active ? "Aktif" : "Nonaktif"), 1)]),
												_: 2
											}, 1032, ["variant"])]),
											createVNode("td", { class: "px-4 py-3 text-right" }, [createVNode("div", { class: "flex justify-end gap-2" }, [createVNode(_sfc_main$1, {
												variant: "ghost",
												size: "icon",
												onClick: ($event) => openEdit(v)
											}, {
												default: withCtx(() => [createVNode(unref(Pencil), { class: "h-4 w-4" })]),
												_: 1
											}, 8, ["onClick"]), createVNode(_sfc_main$1, {
												variant: "ghost",
												size: "icon",
												class: "text-destructive",
												onClick: ($event) => doDelete(v)
											}, {
												default: withCtx(() => [createVNode(unref(Trash2), { class: "h-4 w-4" })]),
												_: 1
											}, 8, ["onClick"])])])
										]);
									}), 128))])])])), filteredVouchers.value.length > 0 && !loading.value ? (openBlock(), createBlock(_sfc_main$9, {
										key: 3,
										page: page.value,
										"page-size": pageSize.value,
										total: filteredVouchers.value.length,
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
									_: 1
								})];
							}),
							_: 1
						}, _parent, _scopeId));
						_push(`</div>`);
						ssrRenderTeleport(_push, (_push) => {
							if (showDrawer.value) _push(`<div class="fixed inset-0 z-[100] bg-black/40 backdrop-blur-sm" data-v-0d2cdf65${_scopeId}></div>`);
							else _push(`<!---->`);
							if (showDrawer.value) {
								_push(`<div class="fixed inset-y-0 right-0 z-[101] flex flex-col w-full sm:max-w-[480px] h-full bg-card shadow-2xl sm:border-l overflow-hidden" data-v-0d2cdf65${_scopeId}><div class="flex items-center justify-between px-6 py-4 border-b shrink-0" data-v-0d2cdf65${_scopeId}><div data-v-0d2cdf65${_scopeId}><h3 class="font-semibold text-base" data-v-0d2cdf65${_scopeId}>${ssrInterpolate(modalMode.value === "create" ? "Tambah Voucer" : "Edit Voucer")}</h3><p class="text-xs text-muted-foreground mt-0.5" data-v-0d2cdf65${_scopeId}>Atur kode promo dan detail diskon.</p></div>`);
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
								_push(`</div><div class="flex-1 overflow-y-auto px-6 py-5 space-y-4 custom-scrollbar" data-v-0d2cdf65${_scopeId}>`);
								if (formError.value) _push(ssrRenderComponent(_sfc_main$6, { variant: "destructive" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`${ssrInterpolate(formError.value)}`);
										else return [createTextVNode(toDisplayString(formError.value), 1)];
									}),
									_: 1
								}, _parent, _scopeId));
								else _push(`<!---->`);
								_push(`<div class="grid grid-cols-2 gap-4" data-v-0d2cdf65${_scopeId}><div class="space-y-1.5" data-v-0d2cdf65${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, { for: "code" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Kode Voucer <span class="text-destructive" data-v-0d2cdf65${_scopeId}>*</span>`);
										else return [createTextVNode("Kode Voucer "), createVNode("span", { class: "text-destructive" }, "*")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$5, {
									id: "code",
									modelValue: form.value.code,
									"onUpdate:modelValue": ($event) => form.value.code = $event,
									placeholder: "CONTOH: PROMO10",
									class: "font-mono uppercase"
								}, null, _parent, _scopeId));
								_push(`</div><div class="space-y-1.5" data-v-0d2cdf65${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, { for: "partner" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Partner <span class="text-destructive" data-v-0d2cdf65${_scopeId}>*</span>`);
										else return [createTextVNode("Partner "), createVNode("span", { class: "text-destructive" }, "*")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`<select id="partner" class="w-full h-10 rounded-md border border-input bg-background px-3 text-sm" data-v-0d2cdf65${_scopeId}><option value="" disabled data-v-0d2cdf65${ssrIncludeBooleanAttr(Array.isArray(form.value.partner_id) ? ssrLooseContain(form.value.partner_id, "") : ssrLooseEqual(form.value.partner_id, "")) ? " selected" : ""}${_scopeId}>Pilih Partner</option><!--[-->`);
								ssrRenderList(partners.value, (p) => {
									_push(`<option${ssrRenderAttr("value", p.id)} data-v-0d2cdf65${ssrIncludeBooleanAttr(Array.isArray(form.value.partner_id) ? ssrLooseContain(form.value.partner_id, p.id) : ssrLooseEqual(form.value.partner_id, p.id)) ? " selected" : ""}${_scopeId}>${ssrInterpolate(p.name)}</option>`);
								});
								_push(`<!--]--></select></div></div><div class="space-y-1.5" data-v-0d2cdf65${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, { for: "name" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Nama Voucer <span class="text-destructive" data-v-0d2cdf65${_scopeId}>*</span>`);
										else return [createTextVNode("Nama Voucer "), createVNode("span", { class: "text-destructive" }, "*")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$5, {
									id: "name",
									modelValue: form.value.name,
									"onUpdate:modelValue": ($event) => form.value.name = $event,
									placeholder: "Nama promo..."
								}, null, _parent, _scopeId));
								_push(`</div><div class="grid grid-cols-2 gap-4" data-v-0d2cdf65${_scopeId}><div class="space-y-1.5" data-v-0d2cdf65${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, { for: "discount_type" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Tipe Diskon`);
										else return [createTextVNode("Tipe Diskon")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`<select id="discount_type" class="w-full h-10 rounded-md border border-input bg-background px-3 text-sm" data-v-0d2cdf65${_scopeId}><option value="percent" data-v-0d2cdf65${ssrIncludeBooleanAttr(Array.isArray(form.value.discount_type) ? ssrLooseContain(form.value.discount_type, "percent") : ssrLooseEqual(form.value.discount_type, "percent")) ? " selected" : ""}${_scopeId}>Persentase (%)</option><option value="fixed" data-v-0d2cdf65${ssrIncludeBooleanAttr(Array.isArray(form.value.discount_type) ? ssrLooseContain(form.value.discount_type, "fixed") : ssrLooseEqual(form.value.discount_type, "fixed")) ? " selected" : ""}${_scopeId}>Nominal Tetap (Rp)</option></select></div><div class="space-y-1.5" data-v-0d2cdf65${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, { for: "discount_value" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Nilai Diskon <span class="text-destructive" data-v-0d2cdf65${_scopeId}>*</span>`);
										else return [createTextVNode("Nilai Diskon "), createVNode("span", { class: "text-destructive" }, "*")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$5, {
									id: "discount_value",
									type: "number",
									modelValue: form.value.discount_value,
									"onUpdate:modelValue": ($event) => form.value.discount_value = $event
								}, null, _parent, _scopeId));
								_push(`</div></div><div class="grid grid-cols-2 gap-4" data-v-0d2cdf65${_scopeId}><div class="space-y-1.5" data-v-0d2cdf65${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, { for: "min_purchase" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Min. Pembelian`);
										else return [createTextVNode("Min. Pembelian")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$5, {
									id: "min_purchase",
									type: "number",
									modelValue: form.value.min_purchase,
									"onUpdate:modelValue": ($event) => form.value.min_purchase = $event
								}, null, _parent, _scopeId));
								_push(`</div><div class="space-y-1.5" data-v-0d2cdf65${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, { for: "max_discount" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Maks. Diskon (Cap)`);
										else return [createTextVNode("Maks. Diskon (Cap)")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$5, {
									id: "max_discount",
									type: "number",
									modelValue: form.value.max_discount,
									"onUpdate:modelValue": ($event) => form.value.max_discount = $event,
									placeholder: "Kosongkan jika tidak ada cap"
								}, null, _parent, _scopeId));
								_push(`</div></div><div class="grid grid-cols-2 gap-4" data-v-0d2cdf65${_scopeId}><div class="space-y-1.5" data-v-0d2cdf65${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, { for: "quota" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Kuota`);
										else return [createTextVNode("Kuota")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$5, {
									id: "quota",
									type: "number",
									modelValue: form.value.quota,
									"onUpdate:modelValue": ($event) => form.value.quota = $event,
									placeholder: "Kosongkan jika tidak terbatas"
								}, null, _parent, _scopeId));
								_push(`</div><div class="space-y-1.5 flex flex-col justify-end" data-v-0d2cdf65${_scopeId}><div class="flex items-center space-x-2 h-10" data-v-0d2cdf65${_scopeId}><input type="checkbox" id="is_active"${ssrIncludeBooleanAttr(Array.isArray(form.value.is_active) ? ssrLooseContain(form.value.is_active, null) : form.value.is_active) ? " checked" : ""} class="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary" data-v-0d2cdf65${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, {
									for: "is_active",
									class: "cursor-pointer"
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Status Aktif`);
										else return [createTextVNode("Status Aktif")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`</div></div></div><div class="grid grid-cols-2 gap-4 pt-2" data-v-0d2cdf65${_scopeId}><div class="space-y-1.5" data-v-0d2cdf65${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, { for: "valid_from" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Berlaku Dari`);
										else return [createTextVNode("Berlaku Dari")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$5, {
									id: "valid_from",
									type: "date",
									modelValue: form.value.valid_from,
									"onUpdate:modelValue": ($event) => form.value.valid_from = $event
								}, null, _parent, _scopeId));
								_push(`</div><div class="space-y-1.5" data-v-0d2cdf65${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, { for: "valid_until" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Berlaku Sampai`);
										else return [createTextVNode("Berlaku Sampai")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$5, {
									id: "valid_until",
									type: "date",
									modelValue: form.value.valid_until,
									"onUpdate:modelValue": ($event) => form.value.valid_until = $event
								}, null, _parent, _scopeId));
								_push(`</div></div></div><div class="flex justify-end gap-3 px-6 py-4 border-t bg-muted/30" data-v-0d2cdf65${_scopeId}>`);
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
									onClick: saveVoucher,
									disabled: saving.value
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) {
											if (saving.value) _push(ssrRenderComponent(unref(Loader2), { class: "h-4 w-4 mr-2 animate-spin" }, null, _parent, _scopeId));
											else _push(`<!---->`);
											_push(` Simpan Voucer `);
										} else return [saving.value ? (openBlock(), createBlock(unref(Loader2), {
											key: 0,
											class: "h-4 w-4 mr-2 animate-spin"
										})) : createCommentVNode("", true), createTextVNode(" Simpan Voucer ")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`</div></div>`);
							} else _push(`<!---->`);
						}, "body", false, _parent);
						ssrRenderTeleport(_push, (_push) => {
							if (deleteModal.value.show) _push(`<div class="fixed inset-0 z-[110] bg-black/40 backdrop-blur-sm" data-v-0d2cdf65${_scopeId}></div>`);
							else _push(`<!---->`);
							if (deleteModal.value.show) {
								_push(`<div class="fixed inset-0 z-[111] flex items-center justify-center p-4 pointer-events-none" data-v-0d2cdf65${_scopeId}><div class="relative bg-card rounded-xl shadow-2xl w-full max-w-md overflow-hidden border border-border pointer-events-auto" data-v-0d2cdf65${_scopeId}><div class="p-6" data-v-0d2cdf65${_scopeId}><h3 class="text-lg font-semibold text-destructive flex items-center gap-2" data-v-0d2cdf65${_scopeId}>`);
								_push(ssrRenderComponent(unref(Trash2), { class: "h-5 w-5" }, null, _parent, _scopeId));
								_push(` Hapus Voucer </h3><p class="text-sm text-muted-foreground mt-2" data-v-0d2cdf65${_scopeId}> Tindakan ini tidak dapat dibatalkan. Hal ini akan menghapus voucer <span class="font-semibold text-foreground font-mono" data-v-0d2cdf65${_scopeId}>${ssrInterpolate(deleteModal.value.voucher?.code)}</span> secara permanen. </p><div class="mt-4" data-v-0d2cdf65${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, { class: "text-sm font-medium" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(` Ketik <span class="font-bold select-all bg-muted px-1.5 py-0.5 rounded text-foreground" data-v-0d2cdf65${_scopeId}>${ssrInterpolate(deleteModal.value.voucher?.code)}</span> untuk mengonfirmasi. `);
										else return [
											createTextVNode(" Ketik "),
											createVNode("span", { class: "font-bold select-all bg-muted px-1.5 py-0.5 rounded text-foreground" }, toDisplayString(deleteModal.value.voucher?.code), 1),
											createTextVNode(" untuk mengonfirmasi. ")
										];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$5, {
									modelValue: deleteModal.value.confirmText,
									"onUpdate:modelValue": ($event) => deleteModal.value.confirmText = $event,
									class: "mt-2",
									placeholder: "Masukkan kode voucer",
									onKeyup: confirmDelete
								}, null, _parent, _scopeId));
								_push(`</div></div><div class="flex items-center justify-end gap-3 px-6 py-4 bg-muted/30 border-t" data-v-0d2cdf65${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$1, {
									variant: "outline",
									onClick: closeDeleteModal
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Batal`);
										else return [createTextVNode("Batal")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$1, {
									variant: "destructive",
									disabled: deleteModal.value.confirmText !== deleteModal.value.voucher?.code || deleting.value,
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
					} else return [
						createVNode("div", { class: "pb-6" }, [
							createVNode("div", { class: "mb-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4" }, [createVNode("div", null, [createVNode("h1", { class: "text-xl font-bold tracking-tight text-zinc-900 dark:text-zinc-100" }, "Manajemen Voucer"), createVNode("p", { class: "text-xs text-zinc-500 mt-0.5" }, "Kelola kode promo dan diskon belanja.")]), createVNode(_sfc_main$1, {
								onClick: openCreate,
								class: "bg-primary hover:bg-primary/90 flex items-center gap-2"
							}, {
								default: withCtx(() => [createVNode(unref(Plus), { class: "h-4 w-4" }), createVNode("span", null, "Tambah Voucer")]),
								_: 1
							})]),
							createVNode("div", { class: "flex flex-col sm:flex-row items-center justify-between gap-4 mb-5" }, [createVNode(_sfc_main$8, {
								modelValue: searchQuery.value,
								"onUpdate:modelValue": ($event) => searchQuery.value = $event,
								placeholder: "Cari kode atau nama voucer...",
								class: "w-full sm:max-w-sm"
							}, null, 8, ["modelValue", "onUpdate:modelValue"])]),
							createVNode(_sfc_main$3, null, {
								default: withCtx(() => [createVNode(_sfc_main$4, { class: "p-0" }, {
									default: withCtx(() => [loading.value ? (openBlock(), createBlock("div", {
										key: 0,
										class: "flex items-center justify-center py-20"
									}, [createVNode(unref(Loader2), { class: "h-6 w-6 animate-spin text-muted-foreground" })])) : filteredVouchers.value.length === 0 ? (openBlock(), createBlock("div", {
										key: 1,
										class: "flex flex-col items-center justify-center py-20 text-muted-foreground"
									}, [createVNode(unref(Ticket), { class: "h-10 w-10 mb-3 opacity-20" }), createVNode("p", { class: "text-sm" }, "Belum ada data voucer.")])) : (openBlock(), createBlock("div", {
										key: 2,
										class: "overflow-x-auto"
									}, [createVNode("table", { class: "w-full text-sm" }, [createVNode("thead", null, [createVNode("tr", { class: "bg-muted/40 border-b" }, [
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Voucer"),
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Diskon"),
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Kuota / Terpakai"),
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Masa Berlaku"),
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Status"),
										createVNode("th", { class: "px-4 py-3 text-right font-medium text-muted-foreground" }, "Aksi")
									])]), createVNode("tbody", null, [(openBlock(true), createBlock(Fragment, null, renderList(paginatedVouchers.value, (v) => {
										return openBlock(), createBlock("tr", {
											key: v.id,
											class: "border-b last:border-0 hover:bg-muted/30 transition-colors"
										}, [
											createVNode("td", { class: "px-4 py-3" }, [createVNode("div", { class: "flex flex-col" }, [
												createVNode("span", { class: "font-mono text-xs font-bold text-primary" }, toDisplayString(v.code), 1),
												createVNode("span", { class: "font-medium" }, toDisplayString(v.name), 1),
												createVNode("span", { class: "text-[10px] text-muted-foreground" }, toDisplayString(getPartnerName(v.partner_id)), 1)
											])]),
											createVNode("td", { class: "px-4 py-3" }, [createVNode("div", { class: "flex flex-col" }, [createVNode("span", { class: "font-semibold" }, toDisplayString(v.discount_type === "percent" ? v.discount_value + "%" : formatCurrency(v.discount_value)), 1), v.min_purchase > 0 ? (openBlock(), createBlock("span", {
												key: 0,
												class: "text-[10px] text-muted-foreground"
											}, " Min. " + toDisplayString(formatCurrency(v.min_purchase)), 1)) : createCommentVNode("", true)])]),
											createVNode("td", { class: "px-4 py-3" }, [createVNode("div", { class: "flex flex-col" }, [createVNode("span", null, toDisplayString(v.quota || "∞") + " / " + toDisplayString(v.used_count || 0), 1), createVNode("div", { class: "w-24 h-1.5 bg-muted rounded-full mt-1 overflow-hidden" }, [createVNode("div", {
												class: "h-full bg-primary",
												style: { width: v.quota ? Math.min(v.used_count / v.quota * 100, 100) + "%" : "0%" }
											}, null, 4)])])]),
											createVNode("td", { class: "px-4 py-3 text-xs" }, [createVNode("div", { class: "flex flex-col gap-0.5" }, [createVNode("div", { class: "flex items-center gap-1" }, [createVNode(unref(Calendar), { class: "h-3 w-3 text-muted-foreground" }), createVNode("span", null, toDisplayString(v.valid_from || "-"), 1)]), createVNode("div", { class: "flex items-center gap-1" }, [createVNode(unref(Calendar), { class: "h-3 w-3 text-muted-foreground" }), createVNode("span", null, toDisplayString(v.valid_until || "-"), 1)])])]),
											createVNode("td", { class: "px-4 py-3" }, [createVNode(_sfc_main$10, { variant: v.is_active ? "default" : "secondary" }, {
												default: withCtx(() => [createTextVNode(toDisplayString(v.is_active ? "Aktif" : "Nonaktif"), 1)]),
												_: 2
											}, 1032, ["variant"])]),
											createVNode("td", { class: "px-4 py-3 text-right" }, [createVNode("div", { class: "flex justify-end gap-2" }, [createVNode(_sfc_main$1, {
												variant: "ghost",
												size: "icon",
												onClick: ($event) => openEdit(v)
											}, {
												default: withCtx(() => [createVNode(unref(Pencil), { class: "h-4 w-4" })]),
												_: 1
											}, 8, ["onClick"]), createVNode(_sfc_main$1, {
												variant: "ghost",
												size: "icon",
												class: "text-destructive",
												onClick: ($event) => doDelete(v)
											}, {
												default: withCtx(() => [createVNode(unref(Trash2), { class: "h-4 w-4" })]),
												_: 1
											}, 8, ["onClick"])])])
										]);
									}), 128))])])])), filteredVouchers.value.length > 0 && !loading.value ? (openBlock(), createBlock(_sfc_main$9, {
										key: 3,
										page: page.value,
										"page-size": pageSize.value,
										total: filteredVouchers.value.length,
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
									_: 1
								})]),
								_: 1
							})
						]),
						(openBlock(), createBlock(Teleport, { to: "body" }, [createVNode(Transition, { name: "fade" }, {
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
								createVNode("div", { class: "flex items-center justify-between px-6 py-4 border-b shrink-0" }, [createVNode("div", null, [createVNode("h3", { class: "font-semibold text-base" }, toDisplayString(modalMode.value === "create" ? "Tambah Voucer" : "Edit Voucer"), 1), createVNode("p", { class: "text-xs text-muted-foreground mt-0.5" }, "Atur kode promo dan detail diskon.")]), createVNode(_sfc_main$1, {
									variant: "ghost",
									size: "icon",
									onClick: ($event) => showDrawer.value = false
								}, {
									default: withCtx(() => [createVNode(unref(X), { class: "h-4 w-4" })]),
									_: 1
								}, 8, ["onClick"])]),
								createVNode("div", { class: "flex-1 overflow-y-auto px-6 py-5 space-y-4 custom-scrollbar" }, [
									formError.value ? (openBlock(), createBlock(_sfc_main$6, {
										key: 0,
										variant: "destructive"
									}, {
										default: withCtx(() => [createTextVNode(toDisplayString(formError.value), 1)]),
										_: 1
									})) : createCommentVNode("", true),
									createVNode("div", { class: "grid grid-cols-2 gap-4" }, [createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, { for: "code" }, {
										default: withCtx(() => [createTextVNode("Kode Voucer "), createVNode("span", { class: "text-destructive" }, "*")]),
										_: 1
									}), createVNode(_sfc_main$5, {
										id: "code",
										modelValue: form.value.code,
										"onUpdate:modelValue": ($event) => form.value.code = $event,
										placeholder: "CONTOH: PROMO10",
										class: "font-mono uppercase"
									}, null, 8, ["modelValue", "onUpdate:modelValue"])]), createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, { for: "partner" }, {
										default: withCtx(() => [createTextVNode("Partner "), createVNode("span", { class: "text-destructive" }, "*")]),
										_: 1
									}), withDirectives(createVNode("select", {
										id: "partner",
										"onUpdate:modelValue": ($event) => form.value.partner_id = $event,
										class: "w-full h-10 rounded-md border border-input bg-background px-3 text-sm"
									}, [createVNode("option", {
										value: "",
										disabled: ""
									}, "Pilih Partner"), (openBlock(true), createBlock(Fragment, null, renderList(partners.value, (p) => {
										return openBlock(), createBlock("option", {
											key: p.id,
											value: p.id
										}, toDisplayString(p.name), 9, ["value"]);
									}), 128))], 8, ["onUpdate:modelValue"]), [[vModelSelect, form.value.partner_id]])])]),
									createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, { for: "name" }, {
										default: withCtx(() => [createTextVNode("Nama Voucer "), createVNode("span", { class: "text-destructive" }, "*")]),
										_: 1
									}), createVNode(_sfc_main$5, {
										id: "name",
										modelValue: form.value.name,
										"onUpdate:modelValue": ($event) => form.value.name = $event,
										placeholder: "Nama promo..."
									}, null, 8, ["modelValue", "onUpdate:modelValue"])]),
									createVNode("div", { class: "grid grid-cols-2 gap-4" }, [createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, { for: "discount_type" }, {
										default: withCtx(() => [createTextVNode("Tipe Diskon")]),
										_: 1
									}), withDirectives(createVNode("select", {
										id: "discount_type",
										"onUpdate:modelValue": ($event) => form.value.discount_type = $event,
										class: "w-full h-10 rounded-md border border-input bg-background px-3 text-sm"
									}, [createVNode("option", { value: "percent" }, "Persentase (%)"), createVNode("option", { value: "fixed" }, "Nominal Tetap (Rp)")], 8, ["onUpdate:modelValue"]), [[vModelSelect, form.value.discount_type]])]), createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, { for: "discount_value" }, {
										default: withCtx(() => [createTextVNode("Nilai Diskon "), createVNode("span", { class: "text-destructive" }, "*")]),
										_: 1
									}), createVNode(_sfc_main$5, {
										id: "discount_value",
										type: "number",
										modelValue: form.value.discount_value,
										"onUpdate:modelValue": ($event) => form.value.discount_value = $event
									}, null, 8, ["modelValue", "onUpdate:modelValue"])])]),
									createVNode("div", { class: "grid grid-cols-2 gap-4" }, [createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, { for: "min_purchase" }, {
										default: withCtx(() => [createTextVNode("Min. Pembelian")]),
										_: 1
									}), createVNode(_sfc_main$5, {
										id: "min_purchase",
										type: "number",
										modelValue: form.value.min_purchase,
										"onUpdate:modelValue": ($event) => form.value.min_purchase = $event
									}, null, 8, ["modelValue", "onUpdate:modelValue"])]), createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, { for: "max_discount" }, {
										default: withCtx(() => [createTextVNode("Maks. Diskon (Cap)")]),
										_: 1
									}), createVNode(_sfc_main$5, {
										id: "max_discount",
										type: "number",
										modelValue: form.value.max_discount,
										"onUpdate:modelValue": ($event) => form.value.max_discount = $event,
										placeholder: "Kosongkan jika tidak ada cap"
									}, null, 8, ["modelValue", "onUpdate:modelValue"])])]),
									createVNode("div", { class: "grid grid-cols-2 gap-4" }, [createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, { for: "quota" }, {
										default: withCtx(() => [createTextVNode("Kuota")]),
										_: 1
									}), createVNode(_sfc_main$5, {
										id: "quota",
										type: "number",
										modelValue: form.value.quota,
										"onUpdate:modelValue": ($event) => form.value.quota = $event,
										placeholder: "Kosongkan jika tidak terbatas"
									}, null, 8, ["modelValue", "onUpdate:modelValue"])]), createVNode("div", { class: "space-y-1.5 flex flex-col justify-end" }, [createVNode("div", { class: "flex items-center space-x-2 h-10" }, [withDirectives(createVNode("input", {
										type: "checkbox",
										id: "is_active",
										"onUpdate:modelValue": ($event) => form.value.is_active = $event,
										class: "h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
									}, null, 8, ["onUpdate:modelValue"]), [[vModelCheckbox, form.value.is_active]]), createVNode(_sfc_main$7, {
										for: "is_active",
										class: "cursor-pointer"
									}, {
										default: withCtx(() => [createTextVNode("Status Aktif")]),
										_: 1
									})])])]),
									createVNode("div", { class: "grid grid-cols-2 gap-4 pt-2" }, [createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, { for: "valid_from" }, {
										default: withCtx(() => [createTextVNode("Berlaku Dari")]),
										_: 1
									}), createVNode(_sfc_main$5, {
										id: "valid_from",
										type: "date",
										modelValue: form.value.valid_from,
										"onUpdate:modelValue": ($event) => form.value.valid_from = $event
									}, null, 8, ["modelValue", "onUpdate:modelValue"])]), createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, { for: "valid_until" }, {
										default: withCtx(() => [createTextVNode("Berlaku Sampai")]),
										_: 1
									}), createVNode(_sfc_main$5, {
										id: "valid_until",
										type: "date",
										modelValue: form.value.valid_until,
										"onUpdate:modelValue": ($event) => form.value.valid_until = $event
									}, null, 8, ["modelValue", "onUpdate:modelValue"])])])
								]),
								createVNode("div", { class: "flex justify-end gap-3 px-6 py-4 border-t bg-muted/30" }, [createVNode(_sfc_main$1, {
									variant: "outline",
									onClick: ($event) => showDrawer.value = false,
									disabled: saving.value
								}, {
									default: withCtx(() => [createTextVNode("Batal")]),
									_: 1
								}, 8, ["onClick", "disabled"]), createVNode(_sfc_main$1, {
									onClick: saveVoucher,
									disabled: saving.value
								}, {
									default: withCtx(() => [saving.value ? (openBlock(), createBlock(unref(Loader2), {
										key: 0,
										class: "h-4 w-4 mr-2 animate-spin"
									})) : createCommentVNode("", true), createTextVNode(" Simpan Voucer ")]),
									_: 1
								}, 8, ["disabled"])])
							])) : createCommentVNode("", true)]),
							_: 1
						})])),
						(openBlock(), createBlock(Teleport, { to: "body" }, [createVNode(Transition, { name: "fade" }, {
							default: withCtx(() => [deleteModal.value.show ? (openBlock(), createBlock("div", {
								key: 0,
								class: "fixed inset-0 z-[110] bg-black/40 backdrop-blur-sm",
								onClick: closeDeleteModal
							})) : createCommentVNode("", true)]),
							_: 1
						}), createVNode(Transition, { name: "scale" }, {
							default: withCtx(() => [deleteModal.value.show ? (openBlock(), createBlock("div", {
								key: 0,
								class: "fixed inset-0 z-[111] flex items-center justify-center p-4 pointer-events-none"
							}, [createVNode("div", { class: "relative bg-card rounded-xl shadow-2xl w-full max-w-md overflow-hidden border border-border pointer-events-auto" }, [createVNode("div", { class: "p-6" }, [
								createVNode("h3", { class: "text-lg font-semibold text-destructive flex items-center gap-2" }, [createVNode(unref(Trash2), { class: "h-5 w-5" }), createTextVNode(" Hapus Voucer ")]),
								createVNode("p", { class: "text-sm text-muted-foreground mt-2" }, [
									createTextVNode(" Tindakan ini tidak dapat dibatalkan. Hal ini akan menghapus voucer "),
									createVNode("span", { class: "font-semibold text-foreground font-mono" }, toDisplayString(deleteModal.value.voucher?.code), 1),
									createTextVNode(" secara permanen. ")
								]),
								createVNode("div", { class: "mt-4" }, [createVNode(_sfc_main$7, { class: "text-sm font-medium" }, {
									default: withCtx(() => [
										createTextVNode(" Ketik "),
										createVNode("span", { class: "font-bold select-all bg-muted px-1.5 py-0.5 rounded text-foreground" }, toDisplayString(deleteModal.value.voucher?.code), 1),
										createTextVNode(" untuk mengonfirmasi. ")
									]),
									_: 1
								}), createVNode(_sfc_main$5, {
									modelValue: deleteModal.value.confirmText,
									"onUpdate:modelValue": ($event) => deleteModal.value.confirmText = $event,
									class: "mt-2",
									placeholder: "Masukkan kode voucer",
									onKeyup: withKeys(confirmDelete, ["enter"])
								}, null, 8, ["modelValue", "onUpdate:modelValue"])])
							]), createVNode("div", { class: "flex items-center justify-end gap-3 px-6 py-4 bg-muted/30 border-t" }, [createVNode(_sfc_main$1, {
								variant: "outline",
								onClick: closeDeleteModal
							}, {
								default: withCtx(() => [createTextVNode("Batal")]),
								_: 1
							}), createVNode(_sfc_main$1, {
								variant: "destructive",
								disabled: deleteModal.value.confirmText !== deleteModal.value.voucher?.code || deleting.value,
								onClick: confirmDelete
							}, {
								default: withCtx(() => [deleting.value ? (openBlock(), createBlock(unref(Loader2), {
									key: 0,
									class: "h-4 w-4 mr-2 animate-spin"
								})) : createCommentVNode("", true), createTextVNode(" Hapus Sekarang ")]),
								_: 1
							}, 8, ["disabled"])])])])) : createCommentVNode("", true)]),
							_: 1
						})]))
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
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/pages/VouchersPage.vue");
	return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
var VouchersPage_default = /* @__PURE__ */ _plugin_vue_export_helper_default(_sfc_main, [["__scopeId", "data-v-0d2cdf65"]]);
//#endregion
export { VouchersPage_default as default };
