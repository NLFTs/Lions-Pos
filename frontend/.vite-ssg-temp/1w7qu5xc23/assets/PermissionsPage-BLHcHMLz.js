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
import { Fragment, Teleport, Transition, computed, createBlock, createCommentVNode, createTextVNode, createVNode, onMounted, openBlock, ref, renderList, toDisplayString, unref, useSSRContext, vModelSelect, watch, withCtx, withDirectives } from "vue";
import { ssrIncludeBooleanAttr, ssrInterpolate, ssrLooseContain, ssrLooseEqual, ssrRenderAttr, ssrRenderClass, ssrRenderComponent, ssrRenderList, ssrRenderTeleport } from "vue/server-renderer";
import { ChevronDown, KeyRound, LayoutGrid, Loader2, Pencil, Plus, Trash2, X } from "lucide-vue-next";
//#region src/pages/PermissionsPage.vue
var _sfc_main = {
	__name: "PermissionsPage",
	__ssrInlineRender: true,
	setup(__props) {
		const { can } = usePermission();
		const { toast } = useToast();
		const { confirm } = useConfirm();
		const permissions = ref([]);
		const modules = ref([]);
		const loading = ref(false);
		const error = ref(null);
		const filterModule = ref("");
		const searchQuery = ref("");
		const page = ref(1);
		const pageSize = ref(10);
		const showDrawer = ref(false);
		const modalMode = ref("create");
		const saving = ref(false);
		const formError = ref(null);
		const form = ref({
			id: null,
			slug: "",
			name: "",
			moduleId: null
		});
		const filtered = computed(() => {
			let result = permissions.value;
			if (filterModule.value) result = result.filter((p) => p.moduleSlug === filterModule.value);
			if (searchQuery.value) {
				const q = searchQuery.value.toLowerCase();
				result = result.filter((p) => p.name.toLowerCase().includes(q) || p.slug.toLowerCase().includes(q));
			}
			return result;
		});
		const paginatedPermissions = computed(() => {
			const start = (page.value - 1) * pageSize.value;
			return filtered.value.slice(start, start + pageSize.value);
		});
		watch([searchQuery, filterModule], () => {
			page.value = 1;
		});
		async function fetchPermissions() {
			loading.value = true;
			error.value = null;
			try {
				permissions.value = (await api.get("/api/v1/permissions")).data.data;
			} catch (err) {
				error.value = err.response?.data?.message || "Failed to load permissions.";
			} finally {
				loading.value = false;
			}
		}
		async function fetchModules() {
			try {
				modules.value = (await api.get("/api/v1/modules")).data.data;
			} catch {}
		}
		onMounted(() => {
			fetchPermissions();
			fetchModules();
		});
		function openCreate() {
			modalMode.value = "create";
			form.value = {
				id: null,
				slug: "",
				name: "",
				moduleId: null
			};
			formError.value = null;
			showDrawer.value = true;
		}
		function openEdit(perm) {
			modalMode.value = "edit";
			const mod = modules.value.find((m) => m.slug === perm.moduleSlug);
			form.value = {
				id: perm.id,
				slug: perm.slug,
				name: perm.name,
				moduleId: mod?.id ?? null
			};
			formError.value = null;
			showDrawer.value = true;
		}
		function closeDrawer() {
			showDrawer.value = false;
		}
		function onNameInput() {
			if (modalMode.value !== "create") return;
			const mod = modules.value.find((m) => m.id === form.value.moduleId)?.slug ?? "";
			const name = form.value.name.trim().toLowerCase();
			let action = "";
			if (name.startsWith("view all")) action = "index";
			else if (name.startsWith("view") || name.startsWith("show")) action = "show";
			else if (name.startsWith("create")) action = "store";
			else if (name.startsWith("update") || name.startsWith("edit")) action = "update";
			else if (name.startsWith("delete") || name.startsWith("remove")) action = "delete";
			form.value.slug = mod && action ? `${mod}.${action}` : mod ? `${mod}.` : "";
		}
		function onModuleSelect() {
			onNameInput();
		}
		async function savePermission() {
			formError.value = null;
			saving.value = true;
			try {
				if (modalMode.value === "create") {
					await api.post("/api/v1/permissions", {
						slug: form.value.slug,
						name: form.value.name,
						moduleId: form.value.moduleId
					});
					toast.success("Permission created!");
				} else {
					await api.put(`/api/v1/permissions/${form.value.id}`, {
						name: form.value.name,
						moduleId: form.value.moduleId
					});
					toast.success("Permission updated!");
				}
				showDrawer.value = false;
				fetchPermissions();
			} catch (err) {
				formError.value = err.response?.data?.message || "Failed to save permission.";
			} finally {
				saving.value = false;
			}
		}
		async function doDelete(perm) {
			if (!await confirm({
				title: "Delete Permission",
				description: `Are you sure you want to delete "${perm.slug}"? Roles with this permission will lose it immediately.`
			})) return;
			try {
				await api.delete(`/api/v1/permissions/${perm.id}`);
				toast.success("Permission deleted!");
				fetchPermissions();
			} catch (err) {
				toast.error(err.response?.data?.message || "Failed to delete permission.");
			}
		}
		return (_ctx, _push, _parent, _attrs) => {
			_push(ssrRenderComponent(_sfc_main$2, _attrs, {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) {
						_push(`<div class="pb-6" data-v-280a13ce${_scopeId}><div class="mb-6" data-v-280a13ce${_scopeId}><h1 class="text-xl font-bold tracking-tight text-zinc-900" data-v-280a13ce${_scopeId}>Permission Management</h1><p class="text-xs text-zinc-500 mt-0.5" data-v-280a13ce${_scopeId}> Manage granular system permissions. </p></div><div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-5" data-v-280a13ce${_scopeId}>`);
						_push(ssrRenderComponent(_sfc_main$8, {
							modelValue: searchQuery.value,
							"onUpdate:modelValue": ($event) => searchQuery.value = $event,
							placeholder: "Search permissions...",
							class: "w-full max-w-sm",
							"input-class": "h-9 text-xs"
						}, null, _parent, _scopeId));
						_push(`<div class="flex items-center gap-2 w-full sm:w-auto" data-v-280a13ce${_scopeId}>`);
						_push(ssrRenderComponent(_sfc_main$1, {
							variant: "outline",
							size: "sm",
							class: "flex-1 sm:flex-none flex items-center justify-center gap-2 border-zinc-200"
						}, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) {
									_push(ssrRenderComponent(unref(LayoutGrid), { class: "h-3.5 w-3.5" }, null, _parent, _scopeId));
									_push(`<span data-v-280a13ce${_scopeId}>Customize Columns</span>`);
									_push(ssrRenderComponent(unref(ChevronDown), { class: "h-3 w-3 text-zinc-400" }, null, _parent, _scopeId));
								} else return [
									createVNode(unref(LayoutGrid), { class: "h-3.5 w-3.5" }),
									createVNode("span", null, "Customize Columns"),
									createVNode(unref(ChevronDown), { class: "h-3 w-3 text-zinc-400" })
								];
							}),
							_: 1
						}, _parent, _scopeId));
						if (unref(can)("permission.store")) _push(ssrRenderComponent(_sfc_main$1, {
							onClick: openCreate,
							size: "sm",
							class: "flex-1 sm:flex-none bg-primary hover:bg-primary/90 flex items-center justify-center gap-2"
						}, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) {
									_push(ssrRenderComponent(unref(Plus), { class: "h-4 w-4" }, null, _parent, _scopeId));
									_push(`<span data-v-280a13ce${_scopeId}>Add Permission</span>`);
								} else return [createVNode(unref(Plus), { class: "h-4 w-4" }), createVNode("span", null, "Add Permission")];
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
						_push(`<div class="mb-4 flex items-center gap-3" data-v-280a13ce${_scopeId}><span class="text-sm text-muted-foreground" data-v-280a13ce${_scopeId}>Filter by module:</span><div class="flex flex-wrap gap-2" data-v-280a13ce${_scopeId}><button class="${ssrRenderClass([filterModule.value === "" ? "bg-primary text-primary-foreground border-primary" : "bg-background text-muted-foreground hover:bg-accent", "rounded-full px-3 py-1 text-xs font-medium border transition-colors"])}" data-v-280a13ce${_scopeId}>All</button><!--[-->`);
						ssrRenderList(modules.value, (mod) => {
							_push(`<button class="${ssrRenderClass([filterModule.value === mod.slug ? "bg-primary text-primary-foreground border-primary" : "bg-background text-muted-foreground hover:bg-accent", "rounded-full px-3 py-1 text-xs font-medium border capitalize transition-colors"])}" data-v-280a13ce${_scopeId}>${ssrInterpolate(mod.name)}</button>`);
						});
						_push(`<!--]--></div></div>`);
						_push(ssrRenderComponent(_sfc_main$3, null, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) _push(ssrRenderComponent(_sfc_main$4, { class: "p-0" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) {
											if (loading.value) {
												_push(`<div class="flex items-center justify-center py-20" data-v-280a13ce${_scopeId}>`);
												_push(ssrRenderComponent(unref(Loader2), { class: "h-6 w-6 animate-spin text-muted-foreground" }, null, _parent, _scopeId));
												_push(`</div>`);
											} else if (filtered.value.length === 0) {
												_push(`<div class="flex flex-col items-center justify-center py-20 text-muted-foreground" data-v-280a13ce${_scopeId}>`);
												_push(ssrRenderComponent(unref(KeyRound), { class: "h-10 w-10 mb-3 opacity-40" }, null, _parent, _scopeId));
												_push(`<p class="text-sm" data-v-280a13ce${_scopeId}>No permissions found.</p></div>`);
											} else {
												_push(`<div class="overflow-x-auto" data-v-280a13ce${_scopeId}><table class="w-full text-sm" data-v-280a13ce${_scopeId}><thead data-v-280a13ce${_scopeId}><tr class="border-b bg-muted/40" data-v-280a13ce${_scopeId}><th class="px-4 py-3 text-left font-medium text-muted-foreground" data-v-280a13ce${_scopeId}>Slug</th><th class="px-4 py-3 text-left font-medium text-muted-foreground" data-v-280a13ce${_scopeId}>Name</th><th class="px-4 py-3 text-left font-medium text-muted-foreground" data-v-280a13ce${_scopeId}>Module</th><th class="px-4 py-3 text-right font-medium text-muted-foreground" data-v-280a13ce${_scopeId}>Actions</th></tr></thead><tbody data-v-280a13ce${_scopeId}><!--[-->`);
												ssrRenderList(paginatedPermissions.value, (perm) => {
													_push(`<tr class="border-b last:border-0 hover:bg-muted/30 transition-colors" data-v-280a13ce${_scopeId}><td class="px-4 py-3 font-mono text-xs" data-v-280a13ce${_scopeId}>${ssrInterpolate(perm.slug)}</td><td class="px-4 py-3" data-v-280a13ce${_scopeId}>${ssrInterpolate(perm.name)}</td><td class="px-4 py-3" data-v-280a13ce${_scopeId}><span class="inline-block rounded-full bg-muted px-2.5 py-0.5 text-xs capitalize" data-v-280a13ce${_scopeId}>${ssrInterpolate(perm.moduleSlug)}</span></td><td class="px-4 py-3 text-right" data-v-280a13ce${_scopeId}><div class="flex justify-end gap-2" data-v-280a13ce${_scopeId}>`);
													if (unref(can)("permission.update")) _push(ssrRenderComponent(_sfc_main$1, {
														variant: "ghost",
														size: "icon",
														onClick: ($event) => openEdit(perm)
													}, {
														default: withCtx((_, _push, _parent, _scopeId) => {
															if (_push) _push(ssrRenderComponent(unref(Pencil), { class: "h-4 w-4" }, null, _parent, _scopeId));
															else return [createVNode(unref(Pencil), { class: "h-4 w-4" })];
														}),
														_: 2
													}, _parent, _scopeId));
													else _push(`<!---->`);
													if (unref(can)("permission.delete")) _push(ssrRenderComponent(_sfc_main$1, {
														variant: "ghost",
														size: "icon",
														class: "text-destructive hover:text-destructive",
														onClick: ($event) => doDelete(perm)
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
											if (filtered.value.length > 0 && !loading.value) _push(ssrRenderComponent(_sfc_main$9, {
												page: page.value,
												"page-size": pageSize.value,
												total: filtered.value.length,
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
										}, [createVNode(unref(Loader2), { class: "h-6 w-6 animate-spin text-muted-foreground" })])) : filtered.value.length === 0 ? (openBlock(), createBlock("div", {
											key: 1,
											class: "flex flex-col items-center justify-center py-20 text-muted-foreground"
										}, [createVNode(unref(KeyRound), { class: "h-10 w-10 mb-3 opacity-40" }), createVNode("p", { class: "text-sm" }, "No permissions found.")])) : (openBlock(), createBlock("div", {
											key: 2,
											class: "overflow-x-auto"
										}, [createVNode("table", { class: "w-full text-sm" }, [createVNode("thead", null, [createVNode("tr", { class: "border-b bg-muted/40" }, [
											createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Slug"),
											createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Name"),
											createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Module"),
											createVNode("th", { class: "px-4 py-3 text-right font-medium text-muted-foreground" }, "Actions")
										])]), createVNode("tbody", null, [(openBlock(true), createBlock(Fragment, null, renderList(paginatedPermissions.value, (perm) => {
											return openBlock(), createBlock("tr", {
												key: perm.id,
												class: "border-b last:border-0 hover:bg-muted/30 transition-colors"
											}, [
												createVNode("td", { class: "px-4 py-3 font-mono text-xs" }, toDisplayString(perm.slug), 1),
												createVNode("td", { class: "px-4 py-3" }, toDisplayString(perm.name), 1),
												createVNode("td", { class: "px-4 py-3" }, [createVNode("span", { class: "inline-block rounded-full bg-muted px-2.5 py-0.5 text-xs capitalize" }, toDisplayString(perm.moduleSlug), 1)]),
												createVNode("td", { class: "px-4 py-3 text-right" }, [createVNode("div", { class: "flex justify-end gap-2" }, [unref(can)("permission.update") ? (openBlock(), createBlock(_sfc_main$1, {
													key: 0,
													variant: "ghost",
													size: "icon",
													onClick: ($event) => openEdit(perm)
												}, {
													default: withCtx(() => [createVNode(unref(Pencil), { class: "h-4 w-4" })]),
													_: 1
												}, 8, ["onClick"])) : createCommentVNode("", true), unref(can)("permission.delete") ? (openBlock(), createBlock(_sfc_main$1, {
													key: 1,
													variant: "ghost",
													size: "icon",
													class: "text-destructive hover:text-destructive",
													onClick: ($event) => doDelete(perm)
												}, {
													default: withCtx(() => [createVNode(unref(Trash2), { class: "h-4 w-4" })]),
													_: 1
												}, 8, ["onClick"])) : createCommentVNode("", true)])])
											]);
										}), 128))])])])), filtered.value.length > 0 && !loading.value ? (openBlock(), createBlock(_sfc_main$9, {
											key: 3,
											page: page.value,
											"page-size": pageSize.value,
											total: filtered.value.length,
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
									}, [createVNode(unref(Loader2), { class: "h-6 w-6 animate-spin text-muted-foreground" })])) : filtered.value.length === 0 ? (openBlock(), createBlock("div", {
										key: 1,
										class: "flex flex-col items-center justify-center py-20 text-muted-foreground"
									}, [createVNode(unref(KeyRound), { class: "h-10 w-10 mb-3 opacity-40" }), createVNode("p", { class: "text-sm" }, "No permissions found.")])) : (openBlock(), createBlock("div", {
										key: 2,
										class: "overflow-x-auto"
									}, [createVNode("table", { class: "w-full text-sm" }, [createVNode("thead", null, [createVNode("tr", { class: "border-b bg-muted/40" }, [
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Slug"),
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Name"),
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Module"),
										createVNode("th", { class: "px-4 py-3 text-right font-medium text-muted-foreground" }, "Actions")
									])]), createVNode("tbody", null, [(openBlock(true), createBlock(Fragment, null, renderList(paginatedPermissions.value, (perm) => {
										return openBlock(), createBlock("tr", {
											key: perm.id,
											class: "border-b last:border-0 hover:bg-muted/30 transition-colors"
										}, [
											createVNode("td", { class: "px-4 py-3 font-mono text-xs" }, toDisplayString(perm.slug), 1),
											createVNode("td", { class: "px-4 py-3" }, toDisplayString(perm.name), 1),
											createVNode("td", { class: "px-4 py-3" }, [createVNode("span", { class: "inline-block rounded-full bg-muted px-2.5 py-0.5 text-xs capitalize" }, toDisplayString(perm.moduleSlug), 1)]),
											createVNode("td", { class: "px-4 py-3 text-right" }, [createVNode("div", { class: "flex justify-end gap-2" }, [unref(can)("permission.update") ? (openBlock(), createBlock(_sfc_main$1, {
												key: 0,
												variant: "ghost",
												size: "icon",
												onClick: ($event) => openEdit(perm)
											}, {
												default: withCtx(() => [createVNode(unref(Pencil), { class: "h-4 w-4" })]),
												_: 1
											}, 8, ["onClick"])) : createCommentVNode("", true), unref(can)("permission.delete") ? (openBlock(), createBlock(_sfc_main$1, {
												key: 1,
												variant: "ghost",
												size: "icon",
												class: "text-destructive hover:text-destructive",
												onClick: ($event) => doDelete(perm)
											}, {
												default: withCtx(() => [createVNode(unref(Trash2), { class: "h-4 w-4" })]),
												_: 1
											}, 8, ["onClick"])) : createCommentVNode("", true)])])
										]);
									}), 128))])])])), filtered.value.length > 0 && !loading.value ? (openBlock(), createBlock(_sfc_main$9, {
										key: 3,
										page: page.value,
										"page-size": pageSize.value,
										total: filtered.value.length,
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
							if (showDrawer.value) _push(`<div class="fixed inset-0 z-[50] bg-black/40 backdrop-blur-sm" data-v-280a13ce${_scopeId}></div>`);
							else _push(`<!---->`);
							if (showDrawer.value) {
								_push(`<div class="fixed inset-y-0 right-0 z-[50] flex flex-col w-full sm:max-w-[420px] h-full bg-card shadow-2xl sm:border-l overflow-hidden" data-v-280a13ce${_scopeId}><div class="flex items-center justify-between px-6 py-4 border-b shrink-0" data-v-280a13ce${_scopeId}><div data-v-280a13ce${_scopeId}><h3 class="font-semibold text-base" data-v-280a13ce${_scopeId}>${ssrInterpolate(modalMode.value === "create" ? "Tambah Permission" : "Edit Permission")}</h3><p class="text-xs text-muted-foreground mt-0.5" data-v-280a13ce${_scopeId}>${ssrInterpolate(modalMode.value === "create" ? "Isi detail permission baru." : "Perbarui informasi permission.")}</p></div>`);
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
								_push(`</div><div class="flex-1 overflow-y-auto px-6 py-5 space-y-5" data-v-280a13ce${_scopeId}>`);
								if (formError.value) _push(ssrRenderComponent(_sfc_main$6, { variant: "destructive" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`<p class="text-sm" data-v-280a13ce${_scopeId}>${ssrInterpolate(formError.value)}</p>`);
										else return [createVNode("p", { class: "text-sm" }, toDisplayString(formError.value), 1)];
									}),
									_: 1
								}, _parent, _scopeId));
								else _push(`<!---->`);
								_push(`<div class="space-y-1.5" data-v-280a13ce${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, { for: "permModule" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Modul`);
										else return [createTextVNode("Modul")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`<select id="permModule" class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50"${ssrIncludeBooleanAttr(saving.value) ? " disabled" : ""} data-v-280a13ce${_scopeId}><option${ssrRenderAttr("value", null)} disabled data-v-280a13ce${ssrIncludeBooleanAttr(Array.isArray(form.value.moduleId) ? ssrLooseContain(form.value.moduleId, null) : ssrLooseEqual(form.value.moduleId, null)) ? " selected" : ""}${_scopeId}>— Pilih modul —</option><!--[-->`);
								ssrRenderList(modules.value, (mod) => {
									_push(`<option${ssrRenderAttr("value", mod.id)} data-v-280a13ce${ssrIncludeBooleanAttr(Array.isArray(form.value.moduleId) ? ssrLooseContain(form.value.moduleId, mod.id) : ssrLooseEqual(form.value.moduleId, mod.id)) ? " selected" : ""}${_scopeId}>${ssrInterpolate(mod.name)} (${ssrInterpolate(mod.slug)})</option>`);
								});
								_push(`<!--]--></select></div><div class="space-y-1.5" data-v-280a13ce${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, { for: "permName" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Nama Permission <span class="text-destructive" data-v-280a13ce${_scopeId}>*</span>`);
										else return [createTextVNode("Nama Permission "), createVNode("span", { class: "text-destructive" }, "*")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$5, {
									id: "permName",
									modelValue: form.value.name,
									"onUpdate:modelValue": ($event) => form.value.name = $event,
									placeholder: "Contoh: View All Posts",
									onInput: onNameInput,
									disabled: saving.value
								}, null, _parent, _scopeId));
								_push(`</div><div class="space-y-1.5" data-v-280a13ce${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, { for: "permSlug" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(` Slug <span class="ml-1 text-[10px] text-muted-foreground font-normal" data-v-280a13ce${_scopeId}>${ssrInterpolate(modalMode.value === "create" ? "(otomatis, format: modul.aksi)" : "(tidak dapat diubah)")}</span>`);
										else return [createTextVNode(" Slug "), createVNode("span", { class: "ml-1 text-[10px] text-muted-foreground font-normal" }, toDisplayString(modalMode.value === "create" ? "(otomatis, format: modul.aksi)" : "(tidak dapat diubah)"), 1)];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$5, {
									id: "permSlug",
									modelValue: form.value.slug,
									"onUpdate:modelValue": ($event) => form.value.slug = $event,
									disabled: modalMode.value === "edit" || saving.value,
									placeholder: "Contoh: post.index",
									class: ["font-mono text-xs", modalMode.value === "edit" ? "bg-muted" : ""]
								}, null, _parent, _scopeId));
								_push(`</div></div><div class="flex justify-end gap-3 px-6 py-4 border-t shrink-0 bg-muted/30" data-v-280a13ce${_scopeId}>`);
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
									onClick: savePermission,
									disabled: saving.value
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) {
											if (saving.value) _push(ssrRenderComponent(unref(Loader2), { class: "h-4 w-4 mr-2 animate-spin" }, null, _parent, _scopeId));
											else _push(`<!---->`);
											_push(` ${ssrInterpolate(modalMode.value === "create" ? "Simpan Permission" : "Simpan Perubahan")}`);
										} else return [saving.value ? (openBlock(), createBlock(unref(Loader2), {
											key: 0,
											class: "h-4 w-4 mr-2 animate-spin"
										})) : createCommentVNode("", true), createTextVNode(" " + toDisplayString(modalMode.value === "create" ? "Simpan Permission" : "Simpan Perubahan"), 1)];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`</div></div>`);
							} else _push(`<!---->`);
						}, "body", false, _parent);
					} else return [createVNode("div", { class: "pb-6" }, [
						createVNode("div", { class: "mb-6" }, [createVNode("h1", { class: "text-xl font-bold tracking-tight text-zinc-900" }, "Permission Management"), createVNode("p", { class: "text-xs text-zinc-500 mt-0.5" }, " Manage granular system permissions. ")]),
						createVNode("div", { class: "flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-5" }, [createVNode(_sfc_main$8, {
							modelValue: searchQuery.value,
							"onUpdate:modelValue": ($event) => searchQuery.value = $event,
							placeholder: "Search permissions...",
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
						}), unref(can)("permission.store") ? (openBlock(), createBlock(_sfc_main$1, {
							key: 0,
							onClick: openCreate,
							size: "sm",
							class: "flex-1 sm:flex-none bg-primary hover:bg-primary/90 flex items-center justify-center gap-2"
						}, {
							default: withCtx(() => [createVNode(unref(Plus), { class: "h-4 w-4" }), createVNode("span", null, "Add Permission")]),
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
						createVNode("div", { class: "mb-4 flex items-center gap-3" }, [createVNode("span", { class: "text-sm text-muted-foreground" }, "Filter by module:"), createVNode("div", { class: "flex flex-wrap gap-2" }, [createVNode("button", {
							class: ["rounded-full px-3 py-1 text-xs font-medium border transition-colors", filterModule.value === "" ? "bg-primary text-primary-foreground border-primary" : "bg-background text-muted-foreground hover:bg-accent"],
							onClick: ($event) => filterModule.value = ""
						}, "All", 10, ["onClick"]), (openBlock(true), createBlock(Fragment, null, renderList(modules.value, (mod) => {
							return openBlock(), createBlock("button", {
								key: mod.slug,
								class: ["rounded-full px-3 py-1 text-xs font-medium border capitalize transition-colors", filterModule.value === mod.slug ? "bg-primary text-primary-foreground border-primary" : "bg-background text-muted-foreground hover:bg-accent"],
								onClick: ($event) => filterModule.value = mod.slug
							}, toDisplayString(mod.name), 11, ["onClick"]);
						}), 128))])]),
						createVNode(_sfc_main$3, null, {
							default: withCtx(() => [createVNode(_sfc_main$4, { class: "p-0" }, {
								default: withCtx(() => [loading.value ? (openBlock(), createBlock("div", {
									key: 0,
									class: "flex items-center justify-center py-20"
								}, [createVNode(unref(Loader2), { class: "h-6 w-6 animate-spin text-muted-foreground" })])) : filtered.value.length === 0 ? (openBlock(), createBlock("div", {
									key: 1,
									class: "flex flex-col items-center justify-center py-20 text-muted-foreground"
								}, [createVNode(unref(KeyRound), { class: "h-10 w-10 mb-3 opacity-40" }), createVNode("p", { class: "text-sm" }, "No permissions found.")])) : (openBlock(), createBlock("div", {
									key: 2,
									class: "overflow-x-auto"
								}, [createVNode("table", { class: "w-full text-sm" }, [createVNode("thead", null, [createVNode("tr", { class: "border-b bg-muted/40" }, [
									createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Slug"),
									createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Name"),
									createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Module"),
									createVNode("th", { class: "px-4 py-3 text-right font-medium text-muted-foreground" }, "Actions")
								])]), createVNode("tbody", null, [(openBlock(true), createBlock(Fragment, null, renderList(paginatedPermissions.value, (perm) => {
									return openBlock(), createBlock("tr", {
										key: perm.id,
										class: "border-b last:border-0 hover:bg-muted/30 transition-colors"
									}, [
										createVNode("td", { class: "px-4 py-3 font-mono text-xs" }, toDisplayString(perm.slug), 1),
										createVNode("td", { class: "px-4 py-3" }, toDisplayString(perm.name), 1),
										createVNode("td", { class: "px-4 py-3" }, [createVNode("span", { class: "inline-block rounded-full bg-muted px-2.5 py-0.5 text-xs capitalize" }, toDisplayString(perm.moduleSlug), 1)]),
										createVNode("td", { class: "px-4 py-3 text-right" }, [createVNode("div", { class: "flex justify-end gap-2" }, [unref(can)("permission.update") ? (openBlock(), createBlock(_sfc_main$1, {
											key: 0,
											variant: "ghost",
											size: "icon",
											onClick: ($event) => openEdit(perm)
										}, {
											default: withCtx(() => [createVNode(unref(Pencil), { class: "h-4 w-4" })]),
											_: 1
										}, 8, ["onClick"])) : createCommentVNode("", true), unref(can)("permission.delete") ? (openBlock(), createBlock(_sfc_main$1, {
											key: 1,
											variant: "ghost",
											size: "icon",
											class: "text-destructive hover:text-destructive",
											onClick: ($event) => doDelete(perm)
										}, {
											default: withCtx(() => [createVNode(unref(Trash2), { class: "h-4 w-4" })]),
											_: 1
										}, 8, ["onClick"])) : createCommentVNode("", true)])])
									]);
								}), 128))])])])), filtered.value.length > 0 && !loading.value ? (openBlock(), createBlock(_sfc_main$9, {
									key: 3,
									page: page.value,
									"page-size": pageSize.value,
									total: filtered.value.length,
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
							createVNode("div", { class: "flex items-center justify-between px-6 py-4 border-b shrink-0" }, [createVNode("div", null, [createVNode("h3", { class: "font-semibold text-base" }, toDisplayString(modalMode.value === "create" ? "Tambah Permission" : "Edit Permission"), 1), createVNode("p", { class: "text-xs text-muted-foreground mt-0.5" }, toDisplayString(modalMode.value === "create" ? "Isi detail permission baru." : "Perbarui informasi permission."), 1)]), createVNode(_sfc_main$1, {
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
								createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, { for: "permModule" }, {
									default: withCtx(() => [createTextVNode("Modul")]),
									_: 1
								}), withDirectives(createVNode("select", {
									id: "permModule",
									"onUpdate:modelValue": ($event) => form.value.moduleId = $event,
									class: "flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50",
									onChange: onModuleSelect,
									disabled: saving.value
								}, [createVNode("option", {
									value: null,
									disabled: ""
								}, "— Pilih modul —"), (openBlock(true), createBlock(Fragment, null, renderList(modules.value, (mod) => {
									return openBlock(), createBlock("option", {
										key: mod.id,
										value: mod.id
									}, toDisplayString(mod.name) + " (" + toDisplayString(mod.slug) + ")", 9, ["value"]);
								}), 128))], 40, ["onUpdate:modelValue", "disabled"]), [[vModelSelect, form.value.moduleId]])]),
								createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, { for: "permName" }, {
									default: withCtx(() => [createTextVNode("Nama Permission "), createVNode("span", { class: "text-destructive" }, "*")]),
									_: 1
								}), createVNode(_sfc_main$5, {
									id: "permName",
									modelValue: form.value.name,
									"onUpdate:modelValue": ($event) => form.value.name = $event,
									placeholder: "Contoh: View All Posts",
									onInput: onNameInput,
									disabled: saving.value
								}, null, 8, [
									"modelValue",
									"onUpdate:modelValue",
									"disabled"
								])]),
								createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, { for: "permSlug" }, {
									default: withCtx(() => [createTextVNode(" Slug "), createVNode("span", { class: "ml-1 text-[10px] text-muted-foreground font-normal" }, toDisplayString(modalMode.value === "create" ? "(otomatis, format: modul.aksi)" : "(tidak dapat diubah)"), 1)]),
									_: 1
								}), createVNode(_sfc_main$5, {
									id: "permSlug",
									modelValue: form.value.slug,
									"onUpdate:modelValue": ($event) => form.value.slug = $event,
									disabled: modalMode.value === "edit" || saving.value,
									placeholder: "Contoh: post.index",
									class: ["font-mono text-xs", modalMode.value === "edit" ? "bg-muted" : ""]
								}, null, 8, [
									"modelValue",
									"onUpdate:modelValue",
									"disabled",
									"class"
								])])
							]),
							createVNode("div", { class: "flex justify-end gap-3 px-6 py-4 border-t shrink-0 bg-muted/30" }, [createVNode(_sfc_main$1, {
								variant: "outline",
								onClick: closeDrawer,
								disabled: saving.value
							}, {
								default: withCtx(() => [createTextVNode("Batal")]),
								_: 1
							}, 8, ["disabled"]), createVNode(_sfc_main$1, {
								onClick: savePermission,
								disabled: saving.value
							}, {
								default: withCtx(() => [saving.value ? (openBlock(), createBlock(unref(Loader2), {
									key: 0,
									class: "h-4 w-4 mr-2 animate-spin"
								})) : createCommentVNode("", true), createTextVNode(" " + toDisplayString(modalMode.value === "create" ? "Simpan Permission" : "Simpan Perubahan"), 1)]),
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
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/pages/PermissionsPage.vue");
	return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
var PermissionsPage_default = /* @__PURE__ */ _plugin_vue_export_helper_default(_sfc_main, [["__scopeId", "data-v-280a13ce"]]);
//#endregion
export { PermissionsPage_default as default };
