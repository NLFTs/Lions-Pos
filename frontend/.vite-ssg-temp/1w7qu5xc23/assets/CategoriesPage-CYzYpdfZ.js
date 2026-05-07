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
import { Fragment, Teleport, Transition, computed, createBlock, createCommentVNode, createTextVNode, createVNode, onMounted, openBlock, ref, renderList, toDisplayString, unref, useSSRContext, vModelText, watch, withCtx, withDirectives } from "vue";
import { ssrIncludeBooleanAttr, ssrInterpolate, ssrRenderComponent, ssrRenderList, ssrRenderTeleport } from "vue/server-renderer";
import { ChevronDown, LayoutGrid, Loader2, Pencil, Plus, Trash2, X } from "lucide-vue-next";
//#region src/pages/CategoriesPage.vue
var _sfc_main = {
	__name: "CategoriesPage",
	__ssrInlineRender: true,
	setup(__props) {
		const { can } = usePermission();
		const { toast } = useToast();
		const { confirm } = useConfirm();
		const categories = ref([]);
		const loading = ref(false);
		const error = ref(null);
		const searchQuery = ref("");
		const page = ref(1);
		const pageSize = ref(10);
		const filteredCategories = computed(() => {
			if (!searchQuery.value) return categories.value;
			const q = searchQuery.value.toLowerCase();
			return categories.value.filter((c) => c.name.toLowerCase().includes(q) || (c.description || "").toLowerCase().includes(q));
		});
		const paginatedCategories = computed(() => {
			const start = (page.value - 1) * pageSize.value;
			return filteredCategories.value.slice(start, start + pageSize.value);
		});
		watch(searchQuery, () => {
			page.value = 1;
		});
		const showDrawer = ref(false);
		const modalMode = ref("create");
		const saving = ref(false);
		const formError = ref(null);
		const form = ref({
			id: null,
			name: "",
			description: ""
		});
		async function fetchCategories() {
			loading.value = true;
			error.value = null;
			try {
				categories.value = (await api.get("/api/v1/categories")).data.data;
			} catch (err) {
				error.value = err.response?.data?.message || "Failed to load categories.";
			} finally {
				loading.value = false;
			}
		}
		onMounted(() => fetchCategories());
		function openCreate() {
			form.value = {
				id: null,
				name: "",
				description: ""
			};
			formError.value = null;
			modalMode.value = "create";
			showDrawer.value = true;
		}
		function closeDrawer() {
			showDrawer.value = false;
		}
		function openEdit(cat) {
			form.value = {
				id: cat.id,
				name: cat.name,
				description: cat.description || ""
			};
			formError.value = null;
			modalMode.value = "edit";
			showDrawer.value = true;
		}
		async function saveCategory() {
			formError.value = null;
			saving.value = true;
			try {
				if (modalMode.value === "create") {
					await api.post("/api/v1/categories", {
						name: form.value.name,
						description: form.value.description
					});
					toast.success("Category created!");
				} else {
					await api.put(`/api/v1/categories/${form.value.id}`, {
						name: form.value.name,
						description: form.value.description
					});
					toast.success("Category updated!");
				}
				showDrawer.value = false;
				fetchCategories();
			} catch (err) {
				formError.value = err.response?.data?.data?.message || err.response?.data?.message || "Failed to save category.";
			} finally {
				saving.value = false;
			}
		}
		async function doDelete(cat) {
			if (!await confirm({
				title: "Delete Category",
				description: `Are you sure you want to delete "${cat.name}"? Posts in this category will be uncategorized.`
			})) return;
			try {
				await api.delete(`/api/v1/categories/${cat.id}`);
				toast.success("Category deleted!");
				fetchCategories();
			} catch (err) {
				toast.error(err.response?.data?.message || "Failed to delete category.");
			}
		}
		function formatDate(dt) {
			if (!dt) return "-";
			return new Date(dt).toLocaleDateString("id-ID", {
				day: "2-digit",
				month: "short",
				year: "numeric"
			});
		}
		return (_ctx, _push, _parent, _attrs) => {
			_push(ssrRenderComponent(_sfc_main$2, _attrs, {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) {
						_push(`<div class="pb-6" data-v-1fbbd0e9${_scopeId}><div class="mb-6" data-v-1fbbd0e9${_scopeId}><h1 class="text-xl font-bold tracking-tight text-zinc-900" data-v-1fbbd0e9${_scopeId}>Category Management</h1><p class="text-xs text-zinc-500 mt-0.5" data-v-1fbbd0e9${_scopeId}> Manage categories for content grouping. </p></div><div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-5" data-v-1fbbd0e9${_scopeId}>`);
						_push(ssrRenderComponent(_sfc_main$8, {
							modelValue: searchQuery.value,
							"onUpdate:modelValue": ($event) => searchQuery.value = $event,
							placeholder: "Search categories...",
							class: "w-full max-w-sm",
							"input-class": "h-9 text-xs"
						}, null, _parent, _scopeId));
						_push(`<div class="flex items-center gap-2 w-full sm:w-auto" data-v-1fbbd0e9${_scopeId}>`);
						_push(ssrRenderComponent(_sfc_main$1, {
							variant: "outline",
							size: "sm",
							class: "flex-1 sm:flex-none flex items-center justify-center gap-2 border-zinc-200"
						}, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) {
									_push(ssrRenderComponent(unref(LayoutGrid), { class: "h-3.5 w-3.5" }, null, _parent, _scopeId));
									_push(`<span data-v-1fbbd0e9${_scopeId}>Customize Columns</span>`);
									_push(ssrRenderComponent(unref(ChevronDown), { class: "h-3 w-3 text-zinc-400" }, null, _parent, _scopeId));
								} else return [
									createVNode(unref(LayoutGrid), { class: "h-3.5 w-3.5" }),
									createVNode("span", null, "Customize Columns"),
									createVNode(unref(ChevronDown), { class: "h-3 w-3 text-zinc-400" })
								];
							}),
							_: 1
						}, _parent, _scopeId));
						if (unref(can)("category.store")) _push(ssrRenderComponent(_sfc_main$1, {
							onClick: openCreate,
							size: "sm",
							class: "flex-1 sm:flex-none bg-primary hover:bg-primary/90 flex items-center justify-center gap-2"
						}, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) {
									_push(ssrRenderComponent(unref(Plus), { class: "h-4 w-4" }, null, _parent, _scopeId));
									_push(`<span data-v-1fbbd0e9${_scopeId}>Add Category</span>`);
								} else return [createVNode(unref(Plus), { class: "h-4 w-4" }), createVNode("span", null, "Add Category")];
							}),
							_: 1
						}, _parent, _scopeId));
						else _push(`<!---->`);
						_push(`</div></div>`);
						if (error.value) _push(ssrRenderComponent(_sfc_main$6, {
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
						_push(ssrRenderComponent(_sfc_main$3, null, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) _push(ssrRenderComponent(_sfc_main$4, { class: "p-0" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) {
											if (loading.value) {
												_push(`<div class="flex items-center justify-center py-20" data-v-1fbbd0e9${_scopeId}>`);
												_push(ssrRenderComponent(unref(Loader2), { class: "h-6 w-6 animate-spin text-muted-foreground" }, null, _parent, _scopeId));
												_push(`</div>`);
											} else if (filteredCategories.value.length === 0) {
												_push(`<div class="flex flex-col items-center justify-center py-20 text-muted-foreground" data-v-1fbbd0e9${_scopeId}><p class="text-sm" data-v-1fbbd0e9${_scopeId}>No categories yet.</p>`);
												if (unref(can)("category.store") && !searchQuery.value) _push(ssrRenderComponent(_sfc_main$1, {
													variant: "outline",
													class: "mt-3",
													onClick: openCreate
												}, {
													default: withCtx((_, _push, _parent, _scopeId) => {
														if (_push) _push(`Create your first category`);
														else return [createTextVNode("Create your first category")];
													}),
													_: 1
												}, _parent, _scopeId));
												else _push(`<!---->`);
												_push(`</div>`);
											} else {
												_push(`<div class="overflow-x-auto" data-v-1fbbd0e9${_scopeId}><table class="w-full text-sm" data-v-1fbbd0e9${_scopeId}><thead data-v-1fbbd0e9${_scopeId}><tr class="border-b bg-muted/40" data-v-1fbbd0e9${_scopeId}><th class="px-4 py-3 text-left font-medium text-muted-foreground w-12" data-v-1fbbd0e9${_scopeId}>#</th><th class="px-4 py-3 text-left font-medium text-muted-foreground" data-v-1fbbd0e9${_scopeId}>Name</th><th class="px-4 py-3 text-left font-medium text-muted-foreground" data-v-1fbbd0e9${_scopeId}>Description</th><th class="px-4 py-3 text-left font-medium text-muted-foreground" data-v-1fbbd0e9${_scopeId}>Created</th><th class="px-4 py-3 text-right font-medium text-muted-foreground" data-v-1fbbd0e9${_scopeId}>Actions</th></tr></thead><tbody data-v-1fbbd0e9${_scopeId}><!--[-->`);
												ssrRenderList(paginatedCategories.value, (cat) => {
													_push(`<tr class="border-b last:border-0 hover:bg-muted/30 transition-colors" data-v-1fbbd0e9${_scopeId}><td class="px-4 py-3 text-muted-foreground" data-v-1fbbd0e9${_scopeId}>${ssrInterpolate(cat.id)}</td><td class="px-4 py-3 font-medium" data-v-1fbbd0e9${_scopeId}>${ssrInterpolate(cat.name)}</td><td class="px-4 py-3 text-muted-foreground max-w-[320px] truncate" data-v-1fbbd0e9${_scopeId}>${ssrInterpolate(cat.description || "-")}</td><td class="px-4 py-3 text-muted-foreground" data-v-1fbbd0e9${_scopeId}>${ssrInterpolate(formatDate(cat.createdAt))}</td><td class="px-4 py-3 text-right" data-v-1fbbd0e9${_scopeId}><div class="flex justify-end gap-2" data-v-1fbbd0e9${_scopeId}>`);
													if (unref(can)("category.update")) _push(ssrRenderComponent(_sfc_main$1, {
														variant: "ghost",
														size: "icon",
														onClick: ($event) => openEdit(cat)
													}, {
														default: withCtx((_, _push, _parent, _scopeId) => {
															if (_push) _push(ssrRenderComponent(unref(Pencil), { class: "h-4 w-4" }, null, _parent, _scopeId));
															else return [createVNode(unref(Pencil), { class: "h-4 w-4" })];
														}),
														_: 2
													}, _parent, _scopeId));
													else _push(`<!---->`);
													if (unref(can)("category.delete")) _push(ssrRenderComponent(_sfc_main$1, {
														variant: "ghost",
														size: "icon",
														class: "text-destructive hover:text-destructive",
														onClick: ($event) => doDelete(cat)
													}, {
														default: withCtx((_, _push, _parent, _scopeId) => {
															if (_push) _push(ssrRenderComponent(unref(Trash2), { class: "h-4 w-4" }, null, _parent, _scopeId));
															else return [createVNode(unref(Trash2), { class: "h-4 w-4" })];
														}),
														_: 2
													}, _parent, _scopeId));
													else _push(`<!---->`);
													_push(`</div></td></tr>`);
												});
												_push(`<!--]--></tbody></table></div>`);
											}
											if (filteredCategories.value.length > 0 && !loading.value) _push(ssrRenderComponent(_sfc_main$9, {
												page: page.value,
												"page-size": pageSize.value,
												total: filteredCategories.value.length,
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
										}, [createVNode(unref(Loader2), { class: "h-6 w-6 animate-spin text-muted-foreground" })])) : filteredCategories.value.length === 0 ? (openBlock(), createBlock("div", {
											key: 1,
											class: "flex flex-col items-center justify-center py-20 text-muted-foreground"
										}, [createVNode("p", { class: "text-sm" }, "No categories yet."), unref(can)("category.store") && !searchQuery.value ? (openBlock(), createBlock(_sfc_main$1, {
											key: 0,
											variant: "outline",
											class: "mt-3",
											onClick: openCreate
										}, {
											default: withCtx(() => [createTextVNode("Create your first category")]),
											_: 1
										})) : createCommentVNode("", true)])) : (openBlock(), createBlock("div", {
											key: 2,
											class: "overflow-x-auto"
										}, [createVNode("table", { class: "w-full text-sm" }, [createVNode("thead", null, [createVNode("tr", { class: "border-b bg-muted/40" }, [
											createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground w-12" }, "#"),
											createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Name"),
											createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Description"),
											createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Created"),
											createVNode("th", { class: "px-4 py-3 text-right font-medium text-muted-foreground" }, "Actions")
										])]), createVNode("tbody", null, [(openBlock(true), createBlock(Fragment, null, renderList(paginatedCategories.value, (cat) => {
											return openBlock(), createBlock("tr", {
												key: cat.id,
												class: "border-b last:border-0 hover:bg-muted/30 transition-colors"
											}, [
												createVNode("td", { class: "px-4 py-3 text-muted-foreground" }, toDisplayString(cat.id), 1),
												createVNode("td", { class: "px-4 py-3 font-medium" }, toDisplayString(cat.name), 1),
												createVNode("td", { class: "px-4 py-3 text-muted-foreground max-w-[320px] truncate" }, toDisplayString(cat.description || "-"), 1),
												createVNode("td", { class: "px-4 py-3 text-muted-foreground" }, toDisplayString(formatDate(cat.createdAt)), 1),
												createVNode("td", { class: "px-4 py-3 text-right" }, [createVNode("div", { class: "flex justify-end gap-2" }, [unref(can)("category.update") ? (openBlock(), createBlock(_sfc_main$1, {
													key: 0,
													variant: "ghost",
													size: "icon",
													onClick: ($event) => openEdit(cat)
												}, {
													default: withCtx(() => [createVNode(unref(Pencil), { class: "h-4 w-4" })]),
													_: 1
												}, 8, ["onClick"])) : createCommentVNode("", true), unref(can)("category.delete") ? (openBlock(), createBlock(_sfc_main$1, {
													key: 1,
													variant: "ghost",
													size: "icon",
													class: "text-destructive hover:text-destructive",
													onClick: ($event) => doDelete(cat)
												}, {
													default: withCtx(() => [createVNode(unref(Trash2), { class: "h-4 w-4" })]),
													_: 1
												}, 8, ["onClick"])) : createCommentVNode("", true)])])
											]);
										}), 128))])])])), filteredCategories.value.length > 0 && !loading.value ? (openBlock(), createBlock(_sfc_main$9, {
											key: 3,
											page: page.value,
											"page-size": pageSize.value,
											total: filteredCategories.value.length,
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
									}, [createVNode(unref(Loader2), { class: "h-6 w-6 animate-spin text-muted-foreground" })])) : filteredCategories.value.length === 0 ? (openBlock(), createBlock("div", {
										key: 1,
										class: "flex flex-col items-center justify-center py-20 text-muted-foreground"
									}, [createVNode("p", { class: "text-sm" }, "No categories yet."), unref(can)("category.store") && !searchQuery.value ? (openBlock(), createBlock(_sfc_main$1, {
										key: 0,
										variant: "outline",
										class: "mt-3",
										onClick: openCreate
									}, {
										default: withCtx(() => [createTextVNode("Create your first category")]),
										_: 1
									})) : createCommentVNode("", true)])) : (openBlock(), createBlock("div", {
										key: 2,
										class: "overflow-x-auto"
									}, [createVNode("table", { class: "w-full text-sm" }, [createVNode("thead", null, [createVNode("tr", { class: "border-b bg-muted/40" }, [
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground w-12" }, "#"),
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Name"),
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Description"),
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Created"),
										createVNode("th", { class: "px-4 py-3 text-right font-medium text-muted-foreground" }, "Actions")
									])]), createVNode("tbody", null, [(openBlock(true), createBlock(Fragment, null, renderList(paginatedCategories.value, (cat) => {
										return openBlock(), createBlock("tr", {
											key: cat.id,
											class: "border-b last:border-0 hover:bg-muted/30 transition-colors"
										}, [
											createVNode("td", { class: "px-4 py-3 text-muted-foreground" }, toDisplayString(cat.id), 1),
											createVNode("td", { class: "px-4 py-3 font-medium" }, toDisplayString(cat.name), 1),
											createVNode("td", { class: "px-4 py-3 text-muted-foreground max-w-[320px] truncate" }, toDisplayString(cat.description || "-"), 1),
											createVNode("td", { class: "px-4 py-3 text-muted-foreground" }, toDisplayString(formatDate(cat.createdAt)), 1),
											createVNode("td", { class: "px-4 py-3 text-right" }, [createVNode("div", { class: "flex justify-end gap-2" }, [unref(can)("category.update") ? (openBlock(), createBlock(_sfc_main$1, {
												key: 0,
												variant: "ghost",
												size: "icon",
												onClick: ($event) => openEdit(cat)
											}, {
												default: withCtx(() => [createVNode(unref(Pencil), { class: "h-4 w-4" })]),
												_: 1
											}, 8, ["onClick"])) : createCommentVNode("", true), unref(can)("category.delete") ? (openBlock(), createBlock(_sfc_main$1, {
												key: 1,
												variant: "ghost",
												size: "icon",
												class: "text-destructive hover:text-destructive",
												onClick: ($event) => doDelete(cat)
											}, {
												default: withCtx(() => [createVNode(unref(Trash2), { class: "h-4 w-4" })]),
												_: 1
											}, 8, ["onClick"])) : createCommentVNode("", true)])])
										]);
									}), 128))])])])), filteredCategories.value.length > 0 && !loading.value ? (openBlock(), createBlock(_sfc_main$9, {
										key: 3,
										page: page.value,
										"page-size": pageSize.value,
										total: filteredCategories.value.length,
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
							if (showDrawer.value) _push(`<div class="fixed inset-0 z-[50] bg-black/40 backdrop-blur-sm" data-v-1fbbd0e9${_scopeId}></div>`);
							else _push(`<!---->`);
							if (showDrawer.value) {
								_push(`<div class="fixed inset-y-0 right-0 z-[50] flex flex-col w-full sm:max-w-[420px] h-full bg-card shadow-2xl sm:border-l overflow-hidden" data-v-1fbbd0e9${_scopeId}><div class="flex items-center justify-between px-6 py-4 border-b shrink-0" data-v-1fbbd0e9${_scopeId}><div data-v-1fbbd0e9${_scopeId}><h3 class="font-semibold text-base" data-v-1fbbd0e9${_scopeId}>${ssrInterpolate(modalMode.value === "create" ? "Tambah Kategori" : "Edit Kategori")}</h3><p class="text-xs text-muted-foreground mt-0.5" data-v-1fbbd0e9${_scopeId}>${ssrInterpolate(modalMode.value === "create" ? "Isi detail kategori baru." : "Perbarui informasi kategori.")}</p></div>`);
								_push(ssrRenderComponent(_sfc_main$1, {
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
								_push(`</div><div class="flex-1 overflow-y-auto px-6 py-5 space-y-5" data-v-1fbbd0e9${_scopeId}>`);
								if (formError.value) _push(ssrRenderComponent(_sfc_main$6, { variant: "destructive" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`<p class="text-sm" data-v-1fbbd0e9${_scopeId}>${ssrInterpolate(formError.value)}</p>`);
										else return [createVNode("p", { class: "text-sm" }, toDisplayString(formError.value), 1)];
									}),
									_: 1
								}, _parent, _scopeId));
								else _push(`<!---->`);
								_push(`<div class="space-y-1.5" data-v-1fbbd0e9${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, { for: "c-name" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Nama Kategori <span class="text-destructive" data-v-1fbbd0e9${_scopeId}>*</span>`);
										else return [createTextVNode("Nama Kategori "), createVNode("span", { class: "text-destructive" }, "*")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$5, {
									id: "c-name",
									modelValue: form.value.name,
									"onUpdate:modelValue": ($event) => form.value.name = $event,
									placeholder: "Nama kategori",
									disabled: saving.value
								}, null, _parent, _scopeId));
								_push(`</div><div class="space-y-1.5" data-v-1fbbd0e9${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, { for: "c-desc" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Deskripsi`);
										else return [createTextVNode("Deskripsi")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`<textarea id="c-desc" rows="4"${ssrIncludeBooleanAttr(saving.value) ? " disabled" : ""} placeholder="Deskripsi opsional..." class="flex min-h-[100px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50 resize-none" data-v-1fbbd0e9${_scopeId}>${ssrInterpolate(form.value.description)}</textarea></div></div><div class="flex justify-end gap-3 px-6 py-4 border-t shrink-0 bg-muted/30" data-v-1fbbd0e9${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$1, {
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
								_push(ssrRenderComponent(_sfc_main$1, {
									onClick: saveCategory,
									disabled: saving.value
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) {
											if (saving.value) _push(ssrRenderComponent(unref(Loader2), { class: "h-4 w-4 mr-2 animate-spin" }, null, _parent, _scopeId));
											else _push(`<!---->`);
											_push(` ${ssrInterpolate(modalMode.value === "create" ? "Simpan Kategori" : "Simpan Perubahan")}`);
										} else return [saving.value ? (openBlock(), createBlock(unref(Loader2), {
											key: 0,
											class: "h-4 w-4 mr-2 animate-spin"
										})) : createCommentVNode("", true), createTextVNode(" " + toDisplayString(modalMode.value === "create" ? "Simpan Kategori" : "Simpan Perubahan"), 1)];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`</div></div>`);
							} else _push(`<!---->`);
						}, "body", false, _parent);
					} else return [createVNode("div", { class: "pb-6" }, [
						createVNode("div", { class: "mb-6" }, [createVNode("h1", { class: "text-xl font-bold tracking-tight text-zinc-900" }, "Category Management"), createVNode("p", { class: "text-xs text-zinc-500 mt-0.5" }, " Manage categories for content grouping. ")]),
						createVNode("div", { class: "flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-5" }, [createVNode(_sfc_main$8, {
							modelValue: searchQuery.value,
							"onUpdate:modelValue": ($event) => searchQuery.value = $event,
							placeholder: "Search categories...",
							class: "w-full max-w-sm",
							"input-class": "h-9 text-xs"
						}, null, 8, ["modelValue", "onUpdate:modelValue"]), createVNode("div", { class: "flex items-center gap-2 w-full sm:w-auto" }, [createVNode(_sfc_main$1, {
							variant: "outline",
							size: "sm",
							class: "flex-1 sm:flex-none flex items-center justify-center gap-2 border-zinc-200"
						}, {
							default: withCtx(() => [
								createVNode(unref(LayoutGrid), { class: "h-3.5 w-3.5" }),
								createVNode("span", null, "Customize Columns"),
								createVNode(unref(ChevronDown), { class: "h-3 w-3 text-zinc-400" })
							]),
							_: 1
						}), unref(can)("category.store") ? (openBlock(), createBlock(_sfc_main$1, {
							key: 0,
							onClick: openCreate,
							size: "sm",
							class: "flex-1 sm:flex-none bg-primary hover:bg-primary/90 flex items-center justify-center gap-2"
						}, {
							default: withCtx(() => [createVNode(unref(Plus), { class: "h-4 w-4" }), createVNode("span", null, "Add Category")]),
							_: 1
						})) : createCommentVNode("", true)])]),
						error.value ? (openBlock(), createBlock(_sfc_main$6, {
							key: 0,
							variant: "destructive",
							class: "mb-4"
						}, {
							default: withCtx(() => [createTextVNode(toDisplayString(error.value), 1)]),
							_: 1
						})) : createCommentVNode("", true),
						createVNode(_sfc_main$3, null, {
							default: withCtx(() => [createVNode(_sfc_main$4, { class: "p-0" }, {
								default: withCtx(() => [loading.value ? (openBlock(), createBlock("div", {
									key: 0,
									class: "flex items-center justify-center py-20"
								}, [createVNode(unref(Loader2), { class: "h-6 w-6 animate-spin text-muted-foreground" })])) : filteredCategories.value.length === 0 ? (openBlock(), createBlock("div", {
									key: 1,
									class: "flex flex-col items-center justify-center py-20 text-muted-foreground"
								}, [createVNode("p", { class: "text-sm" }, "No categories yet."), unref(can)("category.store") && !searchQuery.value ? (openBlock(), createBlock(_sfc_main$1, {
									key: 0,
									variant: "outline",
									class: "mt-3",
									onClick: openCreate
								}, {
									default: withCtx(() => [createTextVNode("Create your first category")]),
									_: 1
								})) : createCommentVNode("", true)])) : (openBlock(), createBlock("div", {
									key: 2,
									class: "overflow-x-auto"
								}, [createVNode("table", { class: "w-full text-sm" }, [createVNode("thead", null, [createVNode("tr", { class: "border-b bg-muted/40" }, [
									createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground w-12" }, "#"),
									createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Name"),
									createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Description"),
									createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Created"),
									createVNode("th", { class: "px-4 py-3 text-right font-medium text-muted-foreground" }, "Actions")
								])]), createVNode("tbody", null, [(openBlock(true), createBlock(Fragment, null, renderList(paginatedCategories.value, (cat) => {
									return openBlock(), createBlock("tr", {
										key: cat.id,
										class: "border-b last:border-0 hover:bg-muted/30 transition-colors"
									}, [
										createVNode("td", { class: "px-4 py-3 text-muted-foreground" }, toDisplayString(cat.id), 1),
										createVNode("td", { class: "px-4 py-3 font-medium" }, toDisplayString(cat.name), 1),
										createVNode("td", { class: "px-4 py-3 text-muted-foreground max-w-[320px] truncate" }, toDisplayString(cat.description || "-"), 1),
										createVNode("td", { class: "px-4 py-3 text-muted-foreground" }, toDisplayString(formatDate(cat.createdAt)), 1),
										createVNode("td", { class: "px-4 py-3 text-right" }, [createVNode("div", { class: "flex justify-end gap-2" }, [unref(can)("category.update") ? (openBlock(), createBlock(_sfc_main$1, {
											key: 0,
											variant: "ghost",
											size: "icon",
											onClick: ($event) => openEdit(cat)
										}, {
											default: withCtx(() => [createVNode(unref(Pencil), { class: "h-4 w-4" })]),
											_: 1
										}, 8, ["onClick"])) : createCommentVNode("", true), unref(can)("category.delete") ? (openBlock(), createBlock(_sfc_main$1, {
											key: 1,
											variant: "ghost",
											size: "icon",
											class: "text-destructive hover:text-destructive",
											onClick: ($event) => doDelete(cat)
										}, {
											default: withCtx(() => [createVNode(unref(Trash2), { class: "h-4 w-4" })]),
											_: 1
										}, 8, ["onClick"])) : createCommentVNode("", true)])])
									]);
								}), 128))])])])), filteredCategories.value.length > 0 && !loading.value ? (openBlock(), createBlock(_sfc_main$9, {
									key: 3,
									page: page.value,
									"page-size": pageSize.value,
									total: filteredCategories.value.length,
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
					]), (openBlock(), createBlock(Teleport, { to: "body" }, [createVNode(Transition, { name: "fade" }, {
						default: withCtx(() => [showDrawer.value ? (openBlock(), createBlock("div", {
							key: 0,
							class: "fixed inset-0 z-[50] bg-black/40 backdrop-blur-sm",
							onClick: closeDrawer
						})) : createCommentVNode("", true)]),
						_: 1
					}), createVNode(Transition, { name: "slide-right" }, {
						default: withCtx(() => [showDrawer.value ? (openBlock(), createBlock("div", {
							key: 0,
							class: "fixed inset-y-0 right-0 z-[50] flex flex-col w-full sm:max-w-[420px] h-full bg-card shadow-2xl sm:border-l overflow-hidden"
						}, [
							createVNode("div", { class: "flex items-center justify-between px-6 py-4 border-b shrink-0" }, [createVNode("div", null, [createVNode("h3", { class: "font-semibold text-base" }, toDisplayString(modalMode.value === "create" ? "Tambah Kategori" : "Edit Kategori"), 1), createVNode("p", { class: "text-xs text-muted-foreground mt-0.5" }, toDisplayString(modalMode.value === "create" ? "Isi detail kategori baru." : "Perbarui informasi kategori."), 1)]), createVNode(_sfc_main$1, {
								variant: "ghost",
								size: "icon",
								onClick: closeDrawer
							}, {
								default: withCtx(() => [createVNode(unref(X), { class: "h-4 w-4" })]),
								_: 1
							})]),
							createVNode("div", { class: "flex-1 overflow-y-auto px-6 py-5 space-y-5" }, [
								formError.value ? (openBlock(), createBlock(_sfc_main$6, {
									key: 0,
									variant: "destructive"
								}, {
									default: withCtx(() => [createVNode("p", { class: "text-sm" }, toDisplayString(formError.value), 1)]),
									_: 1
								})) : createCommentVNode("", true),
								createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, { for: "c-name" }, {
									default: withCtx(() => [createTextVNode("Nama Kategori "), createVNode("span", { class: "text-destructive" }, "*")]),
									_: 1
								}), createVNode(_sfc_main$5, {
									id: "c-name",
									modelValue: form.value.name,
									"onUpdate:modelValue": ($event) => form.value.name = $event,
									placeholder: "Nama kategori",
									disabled: saving.value
								}, null, 8, [
									"modelValue",
									"onUpdate:modelValue",
									"disabled"
								])]),
								createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, { for: "c-desc" }, {
									default: withCtx(() => [createTextVNode("Deskripsi")]),
									_: 1
								}), withDirectives(createVNode("textarea", {
									id: "c-desc",
									"onUpdate:modelValue": ($event) => form.value.description = $event,
									rows: "4",
									disabled: saving.value,
									placeholder: "Deskripsi opsional...",
									class: "flex min-h-[100px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50 resize-none"
								}, null, 8, ["onUpdate:modelValue", "disabled"]), [[vModelText, form.value.description]])])
							]),
							createVNode("div", { class: "flex justify-end gap-3 px-6 py-4 border-t shrink-0 bg-muted/30" }, [createVNode(_sfc_main$1, {
								variant: "outline",
								onClick: closeDrawer,
								disabled: saving.value
							}, {
								default: withCtx(() => [createTextVNode("Batal")]),
								_: 1
							}, 8, ["disabled"]), createVNode(_sfc_main$1, {
								onClick: saveCategory,
								disabled: saving.value
							}, {
								default: withCtx(() => [saving.value ? (openBlock(), createBlock(unref(Loader2), {
									key: 0,
									class: "h-4 w-4 mr-2 animate-spin"
								})) : createCommentVNode("", true), createTextVNode(" " + toDisplayString(modalMode.value === "create" ? "Simpan Kategori" : "Simpan Perubahan"), 1)]),
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
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/pages/CategoriesPage.vue");
	return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
var CategoriesPage_default = /* @__PURE__ */ _plugin_vue_export_helper_default(_sfc_main, [["__scopeId", "data-v-1fbbd0e9"]]);
//#endregion
export { CategoriesPage_default as default };
