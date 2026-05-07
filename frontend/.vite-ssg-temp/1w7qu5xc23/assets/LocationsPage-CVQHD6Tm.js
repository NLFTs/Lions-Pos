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
import { Fragment, Teleport, Transition, computed, createBlock, createCommentVNode, createTextVNode, createVNode, onMounted, openBlock, ref, renderList, toDisplayString, unref, useSSRContext, vModelSelect, vModelText, withCtx, withDirectives, withKeys } from "vue";
import { ssrIncludeBooleanAttr, ssrInterpolate, ssrLooseContain, ssrLooseEqual, ssrRenderComponent, ssrRenderList, ssrRenderTeleport } from "vue/server-renderer";
import { Loader2, MapPin, Pencil, Plus, Trash2, X } from "lucide-vue-next";
//#region src/pages/LocationsPage.vue
var _sfc_main = {
	__name: "LocationsPage",
	__ssrInlineRender: true,
	setup(__props) {
		const { can } = usePermission();
		const { toast } = useToast();
		const { confirm } = useConfirm();
		const locations = ref([]);
		const loading = ref(false);
		const error = ref(null);
		const searchQuery = ref("");
		const page = ref(1);
		const pageSize = ref(10);
		const filteredLocations = computed(() => {
			if (!searchQuery.value) return locations.value;
			const q = searchQuery.value.toLowerCase();
			return locations.value.filter((l) => l.name.toLowerCase().includes(q) || l.type?.toLowerCase().includes(q) || l.address?.toLowerCase().includes(q));
		});
		const paginatedLocations = computed(() => {
			const start = (page.value - 1) * pageSize.value;
			return filteredLocations.value.slice(start, start + pageSize.value);
		});
		const showDrawer = ref(false);
		const modalMode = ref("create");
		const saving = ref(false);
		const formError = ref(null);
		const emptyForm = () => ({
			id: null,
			name: "",
			type: "warehouse",
			address: ""
		});
		const form = ref(emptyForm());
		const deleteModal = ref({
			show: false,
			location: null,
			confirmText: ""
		});
		const deleting = ref(false);
		function doDelete(location) {
			deleteModal.value = {
				show: true,
				location,
				confirmText: ""
			};
		}
		function closeDeleteModal() {
			deleteModal.value.show = false;
			setTimeout(() => {
				deleteModal.value.location = null;
				deleteModal.value.confirmText = "";
			}, 300);
		}
		async function confirmDelete() {
			if (deleteModal.value.confirmText !== deleteModal.value.location?.name) return;
			deleting.value = true;
			try {
				await api.delete(`/api/v1/locations/${deleteModal.value.location.id}`);
				toast.success("Lokasi berhasil dihapus!");
				fetchLocations();
				closeDeleteModal();
			} catch (err) {
				toast.error(err.response?.data?.message || "Gagal menghapus lokasi.");
			} finally {
				deleting.value = false;
			}
		}
		async function fetchLocations() {
			loading.value = true;
			try {
				locations.value = (await api.get("/api/v1/locations")).data.data;
			} catch (err) {
				error.value = "Gagal memuat data lokasi.";
			} finally {
				loading.value = false;
			}
		}
		function openCreate() {
			form.value = emptyForm();
			formError.value = null;
			modalMode.value = "create";
			showDrawer.value = true;
		}
		function openEdit(l) {
			form.value = { ...l };
			formError.value = null;
			modalMode.value = "edit";
			showDrawer.value = true;
		}
		async function saveLocation() {
			saving.value = true;
			formError.value = null;
			try {
				if (modalMode.value === "create") {
					await api.post("/api/v1/locations", form.value);
					toast.success("Lokasi berhasil ditambahkan!");
				} else {
					await api.put(`/api/v1/locations/${form.value.id}`, form.value);
					toast.success("Lokasi berhasil diperbarui!");
				}
				showDrawer.value = false;
				fetchLocations();
			} catch (err) {
				formError.value = err.response?.data?.message || "Gagal menyimpan lokasi.";
			} finally {
				saving.value = false;
			}
		}
		onMounted(fetchLocations);
		return (_ctx, _push, _parent, _attrs) => {
			_push(ssrRenderComponent(_sfc_main$2, _attrs, {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) {
						_push(`<div class="pb-6" data-v-785db3e4${_scopeId}><div class="mb-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4" data-v-785db3e4${_scopeId}><div data-v-785db3e4${_scopeId}><h1 class="text-xl font-bold tracking-tight text-zinc-900 dark:text-zinc-100" data-v-785db3e4${_scopeId}>Manajemen Lokasi</h1><p class="text-xs text-zinc-500 mt-0.5" data-v-785db3e4${_scopeId}>Kelola data gudang dan cabang.</p></div>`);
						_push(ssrRenderComponent(_sfc_main$1, {
							onClick: openCreate,
							class: "bg-primary hover:bg-primary/90 flex items-center gap-2"
						}, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) {
									_push(ssrRenderComponent(unref(Plus), { class: "h-4 w-4" }, null, _parent, _scopeId));
									_push(`<span data-v-785db3e4${_scopeId}>Tambah Lokasi</span>`);
								} else return [createVNode(unref(Plus), { class: "h-4 w-4" }), createVNode("span", null, "Tambah Lokasi")];
							}),
							_: 1
						}, _parent, _scopeId));
						_push(`</div><div class="flex flex-col sm:flex-row items-center justify-between gap-4 mb-5" data-v-785db3e4${_scopeId}>`);
						_push(ssrRenderComponent(_sfc_main$8, {
							modelValue: searchQuery.value,
							"onUpdate:modelValue": ($event) => searchQuery.value = $event,
							placeholder: "Cari lokasi...",
							class: "w-full sm:max-w-sm"
						}, null, _parent, _scopeId));
						_push(`</div>`);
						_push(ssrRenderComponent(_sfc_main$3, null, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) _push(ssrRenderComponent(_sfc_main$4, { class: "p-0" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) {
											if (loading.value) {
												_push(`<div class="flex items-center justify-center py-20" data-v-785db3e4${_scopeId}>`);
												_push(ssrRenderComponent(unref(Loader2), { class: "h-6 w-6 animate-spin text-muted-foreground" }, null, _parent, _scopeId));
												_push(`</div>`);
											} else if (filteredLocations.value.length === 0) {
												_push(`<div class="flex flex-col items-center justify-center py-20 text-muted-foreground" data-v-785db3e4${_scopeId}>`);
												_push(ssrRenderComponent(unref(MapPin), { class: "h-10 w-10 mb-3 opacity-20" }, null, _parent, _scopeId));
												_push(`<p class="text-sm" data-v-785db3e4${_scopeId}>Belum ada data lokasi.</p></div>`);
											} else {
												_push(`<div class="overflow-x-auto" data-v-785db3e4${_scopeId}><table class="w-full text-sm" data-v-785db3e4${_scopeId}><thead data-v-785db3e4${_scopeId}><tr class="bg-muted/40 border-b" data-v-785db3e4${_scopeId}><th class="px-4 py-3 text-left font-medium text-muted-foreground" data-v-785db3e4${_scopeId}>Nama Lokasi</th><th class="px-4 py-3 text-left font-medium text-muted-foreground" data-v-785db3e4${_scopeId}>Tipe</th><th class="px-4 py-3 text-left font-medium text-muted-foreground" data-v-785db3e4${_scopeId}>Alamat</th><th class="px-4 py-3 text-right font-medium text-muted-foreground" data-v-785db3e4${_scopeId}>Aksi</th></tr></thead><tbody data-v-785db3e4${_scopeId}><!--[-->`);
												ssrRenderList(paginatedLocations.value, (l) => {
													_push(`<tr class="border-b last:border-0 hover:bg-muted/30 transition-colors" data-v-785db3e4${_scopeId}><td class="px-4 py-3 font-medium" data-v-785db3e4${_scopeId}>${ssrInterpolate(l.name)}</td><td class="px-4 py-3 capitalize text-xs" data-v-785db3e4${_scopeId}>`);
													_push(ssrRenderComponent(_sfc_main$10, { variant: l.type === "warehouse" ? "default" : "secondary" }, {
														default: withCtx((_, _push, _parent, _scopeId) => {
															if (_push) _push(`${ssrInterpolate(l.type)}`);
															else return [createTextVNode(toDisplayString(l.type), 1)];
														}),
														_: 2
													}, _parent, _scopeId));
													_push(`</td><td class="px-4 py-3 text-xs text-muted-foreground max-w-[250px] truncate" data-v-785db3e4${_scopeId}>${ssrInterpolate(l.address)}</td><td class="px-4 py-3 text-right" data-v-785db3e4${_scopeId}><div class="flex justify-end gap-2" data-v-785db3e4${_scopeId}>`);
													_push(ssrRenderComponent(_sfc_main$1, {
														variant: "ghost",
														size: "icon",
														onClick: ($event) => openEdit(l)
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
														onClick: ($event) => doDelete(l)
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
											if (filteredLocations.value.length > 0 && !loading.value) _push(ssrRenderComponent(_sfc_main$9, {
												page: page.value,
												"page-size": pageSize.value,
												total: filteredLocations.value.length,
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
										}, [createVNode(unref(Loader2), { class: "h-6 w-6 animate-spin text-muted-foreground" })])) : filteredLocations.value.length === 0 ? (openBlock(), createBlock("div", {
											key: 1,
											class: "flex flex-col items-center justify-center py-20 text-muted-foreground"
										}, [createVNode(unref(MapPin), { class: "h-10 w-10 mb-3 opacity-20" }), createVNode("p", { class: "text-sm" }, "Belum ada data lokasi.")])) : (openBlock(), createBlock("div", {
											key: 2,
											class: "overflow-x-auto"
										}, [createVNode("table", { class: "w-full text-sm" }, [createVNode("thead", null, [createVNode("tr", { class: "bg-muted/40 border-b" }, [
											createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Nama Lokasi"),
											createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Tipe"),
											createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Alamat"),
											createVNode("th", { class: "px-4 py-3 text-right font-medium text-muted-foreground" }, "Aksi")
										])]), createVNode("tbody", null, [(openBlock(true), createBlock(Fragment, null, renderList(paginatedLocations.value, (l) => {
											return openBlock(), createBlock("tr", {
												key: l.id,
												class: "border-b last:border-0 hover:bg-muted/30 transition-colors"
											}, [
												createVNode("td", { class: "px-4 py-3 font-medium" }, toDisplayString(l.name), 1),
												createVNode("td", { class: "px-4 py-3 capitalize text-xs" }, [createVNode(_sfc_main$10, { variant: l.type === "warehouse" ? "default" : "secondary" }, {
													default: withCtx(() => [createTextVNode(toDisplayString(l.type), 1)]),
													_: 2
												}, 1032, ["variant"])]),
												createVNode("td", { class: "px-4 py-3 text-xs text-muted-foreground max-w-[250px] truncate" }, toDisplayString(l.address), 1),
												createVNode("td", { class: "px-4 py-3 text-right" }, [createVNode("div", { class: "flex justify-end gap-2" }, [createVNode(_sfc_main$1, {
													variant: "ghost",
													size: "icon",
													onClick: ($event) => openEdit(l)
												}, {
													default: withCtx(() => [createVNode(unref(Pencil), { class: "h-4 w-4" })]),
													_: 1
												}, 8, ["onClick"]), createVNode(_sfc_main$1, {
													variant: "ghost",
													size: "icon",
													class: "text-destructive",
													onClick: ($event) => doDelete(l)
												}, {
													default: withCtx(() => [createVNode(unref(Trash2), { class: "h-4 w-4" })]),
													_: 1
												}, 8, ["onClick"])])])
											]);
										}), 128))])])])), filteredLocations.value.length > 0 && !loading.value ? (openBlock(), createBlock(_sfc_main$9, {
											key: 3,
											page: page.value,
											"page-size": pageSize.value,
											total: filteredLocations.value.length,
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
									}, [createVNode(unref(Loader2), { class: "h-6 w-6 animate-spin text-muted-foreground" })])) : filteredLocations.value.length === 0 ? (openBlock(), createBlock("div", {
										key: 1,
										class: "flex flex-col items-center justify-center py-20 text-muted-foreground"
									}, [createVNode(unref(MapPin), { class: "h-10 w-10 mb-3 opacity-20" }), createVNode("p", { class: "text-sm" }, "Belum ada data lokasi.")])) : (openBlock(), createBlock("div", {
										key: 2,
										class: "overflow-x-auto"
									}, [createVNode("table", { class: "w-full text-sm" }, [createVNode("thead", null, [createVNode("tr", { class: "bg-muted/40 border-b" }, [
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Nama Lokasi"),
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Tipe"),
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Alamat"),
										createVNode("th", { class: "px-4 py-3 text-right font-medium text-muted-foreground" }, "Aksi")
									])]), createVNode("tbody", null, [(openBlock(true), createBlock(Fragment, null, renderList(paginatedLocations.value, (l) => {
										return openBlock(), createBlock("tr", {
											key: l.id,
											class: "border-b last:border-0 hover:bg-muted/30 transition-colors"
										}, [
											createVNode("td", { class: "px-4 py-3 font-medium" }, toDisplayString(l.name), 1),
											createVNode("td", { class: "px-4 py-3 capitalize text-xs" }, [createVNode(_sfc_main$10, { variant: l.type === "warehouse" ? "default" : "secondary" }, {
												default: withCtx(() => [createTextVNode(toDisplayString(l.type), 1)]),
												_: 2
											}, 1032, ["variant"])]),
											createVNode("td", { class: "px-4 py-3 text-xs text-muted-foreground max-w-[250px] truncate" }, toDisplayString(l.address), 1),
											createVNode("td", { class: "px-4 py-3 text-right" }, [createVNode("div", { class: "flex justify-end gap-2" }, [createVNode(_sfc_main$1, {
												variant: "ghost",
												size: "icon",
												onClick: ($event) => openEdit(l)
											}, {
												default: withCtx(() => [createVNode(unref(Pencil), { class: "h-4 w-4" })]),
												_: 1
											}, 8, ["onClick"]), createVNode(_sfc_main$1, {
												variant: "ghost",
												size: "icon",
												class: "text-destructive",
												onClick: ($event) => doDelete(l)
											}, {
												default: withCtx(() => [createVNode(unref(Trash2), { class: "h-4 w-4" })]),
												_: 1
											}, 8, ["onClick"])])])
										]);
									}), 128))])])])), filteredLocations.value.length > 0 && !loading.value ? (openBlock(), createBlock(_sfc_main$9, {
										key: 3,
										page: page.value,
										"page-size": pageSize.value,
										total: filteredLocations.value.length,
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
							if (showDrawer.value) _push(`<div class="fixed inset-0 z-[100] bg-black/40 backdrop-blur-sm" data-v-785db3e4${_scopeId}></div>`);
							else _push(`<!---->`);
							if (showDrawer.value) {
								_push(`<div class="fixed inset-y-0 right-0 z-[101] flex flex-col w-full sm:max-w-[420px] h-full bg-card shadow-2xl sm:border-l overflow-hidden" data-v-785db3e4${_scopeId}><div class="flex items-center justify-between px-6 py-4 border-b shrink-0" data-v-785db3e4${_scopeId}><div data-v-785db3e4${_scopeId}><h3 class="font-semibold text-base" data-v-785db3e4${_scopeId}>${ssrInterpolate(modalMode.value === "create" ? "Tambah Lokasi" : "Edit Lokasi")}</h3><p class="text-xs text-muted-foreground mt-0.5" data-v-785db3e4${_scopeId}>Kelola data gudang atau cabang.</p></div>`);
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
								_push(`</div><div class="flex-1 overflow-y-auto px-6 py-5 space-y-4" data-v-785db3e4${_scopeId}>`);
								if (formError.value) _push(ssrRenderComponent(_sfc_main$6, { variant: "destructive" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`${ssrInterpolate(formError.value)}`);
										else return [createTextVNode(toDisplayString(formError.value), 1)];
									}),
									_: 1
								}, _parent, _scopeId));
								else _push(`<!---->`);
								_push(`<div class="space-y-1.5" data-v-785db3e4${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, { for: "name" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Nama Lokasi <span class="text-destructive" data-v-785db3e4${_scopeId}>*</span>`);
										else return [createTextVNode("Nama Lokasi "), createVNode("span", { class: "text-destructive" }, "*")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$5, {
									id: "name",
									modelValue: form.value.name,
									"onUpdate:modelValue": ($event) => form.value.name = $event,
									placeholder: "Gudang A / Cabang B..."
								}, null, _parent, _scopeId));
								_push(`</div><div class="space-y-1.5" data-v-785db3e4${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, { for: "type" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Tipe Lokasi`);
										else return [createTextVNode("Tipe Lokasi")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`<select id="type" class="w-full h-10 rounded-md border border-input bg-background px-3 text-sm" data-v-785db3e4${_scopeId}><option value="warehouse" data-v-785db3e4${ssrIncludeBooleanAttr(Array.isArray(form.value.type) ? ssrLooseContain(form.value.type, "warehouse") : ssrLooseEqual(form.value.type, "warehouse")) ? " selected" : ""}${_scopeId}>Warehouse (Gudang)</option><option value="branch" data-v-785db3e4${ssrIncludeBooleanAttr(Array.isArray(form.value.type) ? ssrLooseContain(form.value.type, "branch") : ssrLooseEqual(form.value.type, "branch")) ? " selected" : ""}${_scopeId}>Branch (Cabang)</option></select></div><div class="space-y-1.5" data-v-785db3e4${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, { for: "address" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Alamat`);
										else return [createTextVNode("Alamat")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`<textarea id="address" rows="4" class="w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring" placeholder="Alamat lengkap lokasi..." data-v-785db3e4${_scopeId}>${ssrInterpolate(form.value.address)}</textarea></div></div><div class="flex justify-end gap-3 px-6 py-4 border-t bg-muted/30" data-v-785db3e4${_scopeId}>`);
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
									onClick: saveLocation,
									disabled: saving.value
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) {
											if (saving.value) _push(ssrRenderComponent(unref(Loader2), { class: "h-4 w-4 mr-2 animate-spin" }, null, _parent, _scopeId));
											else _push(`<!---->`);
											_push(` Simpan Lokasi `);
										} else return [saving.value ? (openBlock(), createBlock(unref(Loader2), {
											key: 0,
											class: "h-4 w-4 mr-2 animate-spin"
										})) : createCommentVNode("", true), createTextVNode(" Simpan Lokasi ")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`</div></div>`);
							} else _push(`<!---->`);
						}, "body", false, _parent);
						ssrRenderTeleport(_push, (_push) => {
							if (deleteModal.value.show) _push(`<div class="fixed inset-0 z-[110] bg-black/40 backdrop-blur-sm" data-v-785db3e4${_scopeId}></div>`);
							else _push(`<!---->`);
							if (deleteModal.value.show) {
								_push(`<div class="fixed inset-0 z-[111] flex items-center justify-center p-4 pointer-events-none" data-v-785db3e4${_scopeId}><div class="relative bg-card rounded-xl shadow-2xl w-full max-w-md overflow-hidden border border-border pointer-events-auto" data-v-785db3e4${_scopeId}><div class="p-6" data-v-785db3e4${_scopeId}><h3 class="text-lg font-semibold text-destructive flex items-center gap-2" data-v-785db3e4${_scopeId}>`);
								_push(ssrRenderComponent(unref(Trash2), { class: "h-5 w-5" }, null, _parent, _scopeId));
								_push(` Hapus Lokasi </h3><p class="text-sm text-muted-foreground mt-2" data-v-785db3e4${_scopeId}> Tindakan ini tidak dapat dibatalkan. Hal ini akan menghapus lokasi <span class="font-semibold text-foreground" data-v-785db3e4${_scopeId}>${ssrInterpolate(deleteModal.value.location?.name)}</span> secara permanen. </p><div class="mt-4" data-v-785db3e4${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, { class: "text-sm font-medium" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(` Ketik <span class="font-bold select-all bg-muted px-1.5 py-0.5 rounded text-foreground" data-v-785db3e4${_scopeId}>${ssrInterpolate(deleteModal.value.location?.name)}</span> untuk mengonfirmasi. `);
										else return [
											createTextVNode(" Ketik "),
											createVNode("span", { class: "font-bold select-all bg-muted px-1.5 py-0.5 rounded text-foreground" }, toDisplayString(deleteModal.value.location?.name), 1),
											createTextVNode(" untuk mengonfirmasi. ")
										];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$5, {
									modelValue: deleteModal.value.confirmText,
									"onUpdate:modelValue": ($event) => deleteModal.value.confirmText = $event,
									class: "mt-2",
									placeholder: "Masukkan nama lokasi",
									onKeyup: confirmDelete
								}, null, _parent, _scopeId));
								_push(`</div></div><div class="flex items-center justify-end gap-3 px-6 py-4 bg-muted/30 border-t" data-v-785db3e4${_scopeId}>`);
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
									disabled: deleteModal.value.confirmText !== deleteModal.value.location?.name || deleting.value,
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
							createVNode("div", { class: "mb-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4" }, [createVNode("div", null, [createVNode("h1", { class: "text-xl font-bold tracking-tight text-zinc-900 dark:text-zinc-100" }, "Manajemen Lokasi"), createVNode("p", { class: "text-xs text-zinc-500 mt-0.5" }, "Kelola data gudang dan cabang.")]), createVNode(_sfc_main$1, {
								onClick: openCreate,
								class: "bg-primary hover:bg-primary/90 flex items-center gap-2"
							}, {
								default: withCtx(() => [createVNode(unref(Plus), { class: "h-4 w-4" }), createVNode("span", null, "Tambah Lokasi")]),
								_: 1
							})]),
							createVNode("div", { class: "flex flex-col sm:flex-row items-center justify-between gap-4 mb-5" }, [createVNode(_sfc_main$8, {
								modelValue: searchQuery.value,
								"onUpdate:modelValue": ($event) => searchQuery.value = $event,
								placeholder: "Cari lokasi...",
								class: "w-full sm:max-w-sm"
							}, null, 8, ["modelValue", "onUpdate:modelValue"])]),
							createVNode(_sfc_main$3, null, {
								default: withCtx(() => [createVNode(_sfc_main$4, { class: "p-0" }, {
									default: withCtx(() => [loading.value ? (openBlock(), createBlock("div", {
										key: 0,
										class: "flex items-center justify-center py-20"
									}, [createVNode(unref(Loader2), { class: "h-6 w-6 animate-spin text-muted-foreground" })])) : filteredLocations.value.length === 0 ? (openBlock(), createBlock("div", {
										key: 1,
										class: "flex flex-col items-center justify-center py-20 text-muted-foreground"
									}, [createVNode(unref(MapPin), { class: "h-10 w-10 mb-3 opacity-20" }), createVNode("p", { class: "text-sm" }, "Belum ada data lokasi.")])) : (openBlock(), createBlock("div", {
										key: 2,
										class: "overflow-x-auto"
									}, [createVNode("table", { class: "w-full text-sm" }, [createVNode("thead", null, [createVNode("tr", { class: "bg-muted/40 border-b" }, [
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Nama Lokasi"),
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Tipe"),
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Alamat"),
										createVNode("th", { class: "px-4 py-3 text-right font-medium text-muted-foreground" }, "Aksi")
									])]), createVNode("tbody", null, [(openBlock(true), createBlock(Fragment, null, renderList(paginatedLocations.value, (l) => {
										return openBlock(), createBlock("tr", {
											key: l.id,
											class: "border-b last:border-0 hover:bg-muted/30 transition-colors"
										}, [
											createVNode("td", { class: "px-4 py-3 font-medium" }, toDisplayString(l.name), 1),
											createVNode("td", { class: "px-4 py-3 capitalize text-xs" }, [createVNode(_sfc_main$10, { variant: l.type === "warehouse" ? "default" : "secondary" }, {
												default: withCtx(() => [createTextVNode(toDisplayString(l.type), 1)]),
												_: 2
											}, 1032, ["variant"])]),
											createVNode("td", { class: "px-4 py-3 text-xs text-muted-foreground max-w-[250px] truncate" }, toDisplayString(l.address), 1),
											createVNode("td", { class: "px-4 py-3 text-right" }, [createVNode("div", { class: "flex justify-end gap-2" }, [createVNode(_sfc_main$1, {
												variant: "ghost",
												size: "icon",
												onClick: ($event) => openEdit(l)
											}, {
												default: withCtx(() => [createVNode(unref(Pencil), { class: "h-4 w-4" })]),
												_: 1
											}, 8, ["onClick"]), createVNode(_sfc_main$1, {
												variant: "ghost",
												size: "icon",
												class: "text-destructive",
												onClick: ($event) => doDelete(l)
											}, {
												default: withCtx(() => [createVNode(unref(Trash2), { class: "h-4 w-4" })]),
												_: 1
											}, 8, ["onClick"])])])
										]);
									}), 128))])])])), filteredLocations.value.length > 0 && !loading.value ? (openBlock(), createBlock(_sfc_main$9, {
										key: 3,
										page: page.value,
										"page-size": pageSize.value,
										total: filteredLocations.value.length,
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
								class: "fixed inset-y-0 right-0 z-[101] flex flex-col w-full sm:max-w-[420px] h-full bg-card shadow-2xl sm:border-l overflow-hidden"
							}, [
								createVNode("div", { class: "flex items-center justify-between px-6 py-4 border-b shrink-0" }, [createVNode("div", null, [createVNode("h3", { class: "font-semibold text-base" }, toDisplayString(modalMode.value === "create" ? "Tambah Lokasi" : "Edit Lokasi"), 1), createVNode("p", { class: "text-xs text-muted-foreground mt-0.5" }, "Kelola data gudang atau cabang.")]), createVNode(_sfc_main$1, {
									variant: "ghost",
									size: "icon",
									onClick: ($event) => showDrawer.value = false
								}, {
									default: withCtx(() => [createVNode(unref(X), { class: "h-4 w-4" })]),
									_: 1
								}, 8, ["onClick"])]),
								createVNode("div", { class: "flex-1 overflow-y-auto px-6 py-5 space-y-4" }, [
									formError.value ? (openBlock(), createBlock(_sfc_main$6, {
										key: 0,
										variant: "destructive"
									}, {
										default: withCtx(() => [createTextVNode(toDisplayString(formError.value), 1)]),
										_: 1
									})) : createCommentVNode("", true),
									createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, { for: "name" }, {
										default: withCtx(() => [createTextVNode("Nama Lokasi "), createVNode("span", { class: "text-destructive" }, "*")]),
										_: 1
									}), createVNode(_sfc_main$5, {
										id: "name",
										modelValue: form.value.name,
										"onUpdate:modelValue": ($event) => form.value.name = $event,
										placeholder: "Gudang A / Cabang B..."
									}, null, 8, ["modelValue", "onUpdate:modelValue"])]),
									createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, { for: "type" }, {
										default: withCtx(() => [createTextVNode("Tipe Lokasi")]),
										_: 1
									}), withDirectives(createVNode("select", {
										id: "type",
										"onUpdate:modelValue": ($event) => form.value.type = $event,
										class: "w-full h-10 rounded-md border border-input bg-background px-3 text-sm"
									}, [createVNode("option", { value: "warehouse" }, "Warehouse (Gudang)"), createVNode("option", { value: "branch" }, "Branch (Cabang)")], 8, ["onUpdate:modelValue"]), [[vModelSelect, form.value.type]])]),
									createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, { for: "address" }, {
										default: withCtx(() => [createTextVNode("Alamat")]),
										_: 1
									}), withDirectives(createVNode("textarea", {
										id: "address",
										"onUpdate:modelValue": ($event) => form.value.address = $event,
										rows: "4",
										class: "w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring",
										placeholder: "Alamat lengkap lokasi..."
									}, null, 8, ["onUpdate:modelValue"]), [[vModelText, form.value.address]])])
								]),
								createVNode("div", { class: "flex justify-end gap-3 px-6 py-4 border-t bg-muted/30" }, [createVNode(_sfc_main$1, {
									variant: "outline",
									onClick: ($event) => showDrawer.value = false,
									disabled: saving.value
								}, {
									default: withCtx(() => [createTextVNode("Batal")]),
									_: 1
								}, 8, ["onClick", "disabled"]), createVNode(_sfc_main$1, {
									onClick: saveLocation,
									disabled: saving.value
								}, {
									default: withCtx(() => [saving.value ? (openBlock(), createBlock(unref(Loader2), {
										key: 0,
										class: "h-4 w-4 mr-2 animate-spin"
									})) : createCommentVNode("", true), createTextVNode(" Simpan Lokasi ")]),
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
								createVNode("h3", { class: "text-lg font-semibold text-destructive flex items-center gap-2" }, [createVNode(unref(Trash2), { class: "h-5 w-5" }), createTextVNode(" Hapus Lokasi ")]),
								createVNode("p", { class: "text-sm text-muted-foreground mt-2" }, [
									createTextVNode(" Tindakan ini tidak dapat dibatalkan. Hal ini akan menghapus lokasi "),
									createVNode("span", { class: "font-semibold text-foreground" }, toDisplayString(deleteModal.value.location?.name), 1),
									createTextVNode(" secara permanen. ")
								]),
								createVNode("div", { class: "mt-4" }, [createVNode(_sfc_main$7, { class: "text-sm font-medium" }, {
									default: withCtx(() => [
										createTextVNode(" Ketik "),
										createVNode("span", { class: "font-bold select-all bg-muted px-1.5 py-0.5 rounded text-foreground" }, toDisplayString(deleteModal.value.location?.name), 1),
										createTextVNode(" untuk mengonfirmasi. ")
									]),
									_: 1
								}), createVNode(_sfc_main$5, {
									modelValue: deleteModal.value.confirmText,
									"onUpdate:modelValue": ($event) => deleteModal.value.confirmText = $event,
									class: "mt-2",
									placeholder: "Masukkan nama lokasi",
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
								disabled: deleteModal.value.confirmText !== deleteModal.value.location?.name || deleting.value,
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
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/pages/LocationsPage.vue");
	return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
var LocationsPage_default = /* @__PURE__ */ _plugin_vue_export_helper_default(_sfc_main, [["__scopeId", "data-v-785db3e4"]]);
//#endregion
export { LocationsPage_default as default };
