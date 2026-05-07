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
import "./CardTitle-CWxkLjm1.js";
import { Fragment, Teleport, Transition, computed, createBlock, createCommentVNode, createTextVNode, createVNode, onMounted, openBlock, ref, renderList, toDisplayString, unref, useSSRContext, watch, withCtx } from "vue";
import { ssrInterpolate, ssrRenderClass, ssrRenderComponent, ssrRenderList, ssrRenderTeleport } from "vue/server-renderer";
import { Check, ChevronDown, LayoutGrid, Loader2, Pencil, Plus, ShieldCheck, Trash2, X } from "lucide-vue-next";
//#region src/pages/RolesPage.vue
var _sfc_main = {
	__name: "RolesPage",
	__ssrInlineRender: true,
	setup(__props) {
		const { can } = usePermission();
		const { toast } = useToast();
		const { confirm } = useConfirm();
		const roles = ref([]);
		const permissionsMap = ref({});
		const loading = ref(false);
		const error = ref(null);
		const searchQuery = ref("");
		const page = ref(1);
		const pageSize = ref(10);
		const filteredRoles = computed(() => {
			if (!searchQuery.value) return roles.value;
			const q = searchQuery.value.toLowerCase();
			return roles.value.filter((r) => r.name.toLowerCase().includes(q) || r.slug.toLowerCase().includes(q));
		});
		const paginatedRoles = computed(() => {
			const start = (page.value - 1) * pageSize.value;
			return filteredRoles.value.slice(start, start + pageSize.value);
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
			slug: ""
		});
		const selectedPermIds = ref(/* @__PURE__ */ new Set());
		const ACTIONS = [
			"index",
			"show",
			"store",
			"update",
			"delete"
		];
		const modules = computed(() => Object.keys(permissionsMap.value).sort());
		async function fetchRoles() {
			loading.value = true;
			error.value = null;
			try {
				roles.value = (await api.get("/api/v1/roles")).data.data;
			} catch (err) {
				error.value = err.response?.data?.message || "Failed to load roles.";
			} finally {
				loading.value = false;
			}
		}
		async function fetchPermissions() {
			try {
				permissionsMap.value = (await api.get("/api/v1/roles/permissions")).data.data;
			} catch (err) {
				console.error("Failed to load permissions", err);
			}
		}
		onMounted(() => {
			fetchRoles();
			fetchPermissions();
		});
		function getPermission(module, action) {
			return (permissionsMap.value[module] || []).find((p) => p.slug === `${module}.${action}`) || null;
		}
		function isChecked(module, action) {
			const p = getPermission(module, action);
			return p ? selectedPermIds.value.has(p.id) : false;
		}
		function togglePerm(module, action) {
			const p = getPermission(module, action);
			if (!p) return;
			const next = new Set(selectedPermIds.value);
			if (next.has(p.id)) next.delete(p.id);
			else next.add(p.id);
			selectedPermIds.value = next;
		}
		function toggleAllInModule(module) {
			const list = permissionsMap.value[module] || [];
			const allChecked = list.every((p) => selectedPermIds.value.has(p.id));
			const next = new Set(selectedPermIds.value);
			if (allChecked) list.forEach((p) => next.delete(p.id));
			else list.forEach((p) => next.add(p.id));
			selectedPermIds.value = next;
		}
		function isModuleAllChecked(module) {
			const list = permissionsMap.value[module] || [];
			return list.length > 0 && list.every((p) => selectedPermIds.value.has(p.id));
		}
		function permCountFor(role) {
			return role.permissions?.length ?? 0;
		}
		function openCreate() {
			modalMode.value = "create";
			form.value = {
				id: null,
				name: "",
				slug: ""
			};
			selectedPermIds.value = /* @__PURE__ */ new Set();
			formError.value = null;
			showDrawer.value = true;
		}
		function openEdit(role) {
			modalMode.value = "edit";
			form.value = {
				id: role.id,
				name: role.name,
				slug: role.slug
			};
			selectedPermIds.value = new Set((role.permissions || []).map((p) => p.id));
			formError.value = null;
			showDrawer.value = true;
		}
		function closeDrawer() {
			showDrawer.value = false;
		}
		function onNameInput() {
			if (modalMode.value === "create") form.value.slug = form.value.name.toLowerCase().replace(/\s+/g, "-").replace(/[^a-z0-9-]/g, "");
		}
		async function saveRole() {
			formError.value = null;
			saving.value = true;
			try {
				let roleId = form.value.id;
				if (modalMode.value === "create") {
					const payload = {
						name: form.value.name,
						slug: form.value.slug,
						permissionIds: [...selectedPermIds.value]
					};
					roleId = (await api.post("/api/v1/roles", payload)).data.data.id;
					toast.success("Role created!");
				} else {
					await api.put(`/api/v1/roles/${roleId}`, {
						name: form.value.name,
						slug: form.value.slug
					});
					await api.put(`/api/v1/roles/${roleId}/permissions`, { permissionIds: [...selectedPermIds.value] });
					toast.success("Role updated!");
				}
				showDrawer.value = false;
				fetchRoles();
			} catch (err) {
				formError.value = err.response?.data?.message || "Failed to save role.";
			} finally {
				saving.value = false;
			}
		}
		async function doDelete(role) {
			if (!await confirm({
				title: "Delete Role",
				description: `Are you sure you want to delete "${role.name}"? This cannot be undone.`
			})) return;
			try {
				await api.delete(`/api/v1/roles/${role.id}`);
				toast.success("Role deleted!");
				fetchRoles();
			} catch (err) {
				toast.error(err.response?.data?.message || "Failed to delete role.");
			}
		}
		return (_ctx, _push, _parent, _attrs) => {
			_push(ssrRenderComponent(_sfc_main$2, _attrs, {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) {
						_push(`<div class="pb-6" data-v-4172b1b6${_scopeId}><div class="mb-6" data-v-4172b1b6${_scopeId}><h1 class="text-xl font-bold tracking-tight text-zinc-900" data-v-4172b1b6${_scopeId}>Role Management</h1><p class="text-xs text-zinc-500 mt-0.5" data-v-4172b1b6${_scopeId}> Manage system roles and their permissions. </p></div><div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-5" data-v-4172b1b6${_scopeId}>`);
						_push(ssrRenderComponent(_sfc_main$8, {
							modelValue: searchQuery.value,
							"onUpdate:modelValue": ($event) => searchQuery.value = $event,
							placeholder: "Search roles...",
							class: "w-full max-w-sm",
							"input-class": "h-9 text-xs"
						}, null, _parent, _scopeId));
						_push(`<div class="flex items-center gap-2 w-full sm:w-auto" data-v-4172b1b6${_scopeId}>`);
						_push(ssrRenderComponent(_sfc_main$1, {
							variant: "outline",
							size: "sm",
							class: "flex-1 sm:flex-none flex items-center justify-center gap-2 border-zinc-200"
						}, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) {
									_push(ssrRenderComponent(unref(LayoutGrid), { class: "h-3.5 w-3.5" }, null, _parent, _scopeId));
									_push(`<span data-v-4172b1b6${_scopeId}>Customize Columns</span>`);
									_push(ssrRenderComponent(unref(ChevronDown), { class: "h-3 w-3 text-zinc-400" }, null, _parent, _scopeId));
								} else return [
									createVNode(unref(LayoutGrid), { class: "h-3.5 w-3.5" }),
									createVNode("span", null, "Customize Columns"),
									createVNode(unref(ChevronDown), { class: "h-3 w-3 text-zinc-400" })
								];
							}),
							_: 1
						}, _parent, _scopeId));
						if (unref(can)("role.store")) _push(ssrRenderComponent(_sfc_main$1, {
							onClick: openCreate,
							size: "sm",
							class: "flex-1 sm:flex-none bg-primary hover:bg-primary/90 flex items-center justify-center gap-2"
						}, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) {
									_push(ssrRenderComponent(unref(Plus), { class: "h-4 w-4" }, null, _parent, _scopeId));
									_push(`<span data-v-4172b1b6${_scopeId}>Add Role</span>`);
								} else return [createVNode(unref(Plus), { class: "h-4 w-4" }), createVNode("span", null, "Add Role")];
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
												_push(`<div class="flex items-center justify-center py-20" data-v-4172b1b6${_scopeId}>`);
												_push(ssrRenderComponent(unref(Loader2), { class: "h-6 w-6 animate-spin text-muted-foreground" }, null, _parent, _scopeId));
												_push(`</div>`);
											} else if (filteredRoles.value.length === 0) {
												_push(`<div class="flex flex-col items-center justify-center py-20 text-muted-foreground" data-v-4172b1b6${_scopeId}>`);
												_push(ssrRenderComponent(unref(ShieldCheck), { class: "h-10 w-10 mb-3 opacity-40" }, null, _parent, _scopeId));
												_push(`<p class="text-sm" data-v-4172b1b6${_scopeId}>No roles yet.</p></div>`);
											} else {
												_push(`<div class="overflow-x-auto" data-v-4172b1b6${_scopeId}><table class="w-full text-sm" data-v-4172b1b6${_scopeId}><thead data-v-4172b1b6${_scopeId}><tr class="border-b bg-muted/40" data-v-4172b1b6${_scopeId}><th class="px-4 py-3 text-left font-medium text-muted-foreground" data-v-4172b1b6${_scopeId}>Name</th><th class="px-4 py-3 text-left font-medium text-muted-foreground" data-v-4172b1b6${_scopeId}>Slug</th><th class="px-4 py-3 text-left font-medium text-muted-foreground" data-v-4172b1b6${_scopeId}>Permissions</th><th class="px-4 py-3 text-right font-medium text-muted-foreground" data-v-4172b1b6${_scopeId}>Actions</th></tr></thead><tbody data-v-4172b1b6${_scopeId}><!--[-->`);
												ssrRenderList(paginatedRoles.value, (role) => {
													_push(`<tr class="border-b last:border-0 hover:bg-muted/30 transition-colors" data-v-4172b1b6${_scopeId}><td class="px-4 py-3 font-medium" data-v-4172b1b6${_scopeId}>${ssrInterpolate(role.name)}</td><td class="px-4 py-3 font-mono text-xs text-muted-foreground" data-v-4172b1b6${_scopeId}>${ssrInterpolate(role.slug)}</td><td class="px-4 py-3" data-v-4172b1b6${_scopeId}><span class="inline-flex items-center gap-1 rounded-full bg-primary/10 text-primary px-2.5 py-0.5 text-xs font-medium" data-v-4172b1b6${_scopeId}>`);
													_push(ssrRenderComponent(unref(ShieldCheck), { class: "h-3 w-3" }, null, _parent, _scopeId));
													_push(` ${ssrInterpolate(permCountFor(role))} permission${ssrInterpolate(permCountFor(role) !== 1 ? "s" : "")}</span></td><td class="px-4 py-3 text-right" data-v-4172b1b6${_scopeId}><div class="flex justify-end gap-2" data-v-4172b1b6${_scopeId}>`);
													if (unref(can)("role.update")) _push(ssrRenderComponent(_sfc_main$1, {
														variant: "ghost",
														size: "icon",
														onClick: ($event) => openEdit(role)
													}, {
														default: withCtx((_, _push, _parent, _scopeId) => {
															if (_push) _push(ssrRenderComponent(unref(Pencil), { class: "h-4 w-4" }, null, _parent, _scopeId));
															else return [createVNode(unref(Pencil), { class: "h-4 w-4" })];
														}),
														_: 2
													}, _parent, _scopeId));
													else _push(`<!---->`);
													if (unref(can)("role.delete")) _push(ssrRenderComponent(_sfc_main$1, {
														variant: "ghost",
														size: "icon",
														class: "text-destructive hover:text-destructive",
														onClick: ($event) => doDelete(role)
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
											if (filteredRoles.value.length > 0 && !loading.value) _push(ssrRenderComponent(_sfc_main$9, {
												page: page.value,
												"page-size": pageSize.value,
												total: filteredRoles.value.length,
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
										}, [createVNode(unref(Loader2), { class: "h-6 w-6 animate-spin text-muted-foreground" })])) : filteredRoles.value.length === 0 ? (openBlock(), createBlock("div", {
											key: 1,
											class: "flex flex-col items-center justify-center py-20 text-muted-foreground"
										}, [createVNode(unref(ShieldCheck), { class: "h-10 w-10 mb-3 opacity-40" }), createVNode("p", { class: "text-sm" }, "No roles yet.")])) : (openBlock(), createBlock("div", {
											key: 2,
											class: "overflow-x-auto"
										}, [createVNode("table", { class: "w-full text-sm" }, [createVNode("thead", null, [createVNode("tr", { class: "border-b bg-muted/40" }, [
											createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Name"),
											createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Slug"),
											createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Permissions"),
											createVNode("th", { class: "px-4 py-3 text-right font-medium text-muted-foreground" }, "Actions")
										])]), createVNode("tbody", null, [(openBlock(true), createBlock(Fragment, null, renderList(paginatedRoles.value, (role) => {
											return openBlock(), createBlock("tr", {
												key: role.id,
												class: "border-b last:border-0 hover:bg-muted/30 transition-colors"
											}, [
												createVNode("td", { class: "px-4 py-3 font-medium" }, toDisplayString(role.name), 1),
												createVNode("td", { class: "px-4 py-3 font-mono text-xs text-muted-foreground" }, toDisplayString(role.slug), 1),
												createVNode("td", { class: "px-4 py-3" }, [createVNode("span", { class: "inline-flex items-center gap-1 rounded-full bg-primary/10 text-primary px-2.5 py-0.5 text-xs font-medium" }, [createVNode(unref(ShieldCheck), { class: "h-3 w-3" }), createTextVNode(" " + toDisplayString(permCountFor(role)) + " permission" + toDisplayString(permCountFor(role) !== 1 ? "s" : ""), 1)])]),
												createVNode("td", { class: "px-4 py-3 text-right" }, [createVNode("div", { class: "flex justify-end gap-2" }, [unref(can)("role.update") ? (openBlock(), createBlock(_sfc_main$1, {
													key: 0,
													variant: "ghost",
													size: "icon",
													onClick: ($event) => openEdit(role)
												}, {
													default: withCtx(() => [createVNode(unref(Pencil), { class: "h-4 w-4" })]),
													_: 1
												}, 8, ["onClick"])) : createCommentVNode("", true), unref(can)("role.delete") ? (openBlock(), createBlock(_sfc_main$1, {
													key: 1,
													variant: "ghost",
													size: "icon",
													class: "text-destructive hover:text-destructive",
													onClick: ($event) => doDelete(role)
												}, {
													default: withCtx(() => [createVNode(unref(Trash2), { class: "h-4 w-4" })]),
													_: 1
												}, 8, ["onClick"])) : createCommentVNode("", true)])])
											]);
										}), 128))])])])), filteredRoles.value.length > 0 && !loading.value ? (openBlock(), createBlock(_sfc_main$9, {
											key: 3,
											page: page.value,
											"page-size": pageSize.value,
											total: filteredRoles.value.length,
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
									}, [createVNode(unref(Loader2), { class: "h-6 w-6 animate-spin text-muted-foreground" })])) : filteredRoles.value.length === 0 ? (openBlock(), createBlock("div", {
										key: 1,
										class: "flex flex-col items-center justify-center py-20 text-muted-foreground"
									}, [createVNode(unref(ShieldCheck), { class: "h-10 w-10 mb-3 opacity-40" }), createVNode("p", { class: "text-sm" }, "No roles yet.")])) : (openBlock(), createBlock("div", {
										key: 2,
										class: "overflow-x-auto"
									}, [createVNode("table", { class: "w-full text-sm" }, [createVNode("thead", null, [createVNode("tr", { class: "border-b bg-muted/40" }, [
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Name"),
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Slug"),
										createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Permissions"),
										createVNode("th", { class: "px-4 py-3 text-right font-medium text-muted-foreground" }, "Actions")
									])]), createVNode("tbody", null, [(openBlock(true), createBlock(Fragment, null, renderList(paginatedRoles.value, (role) => {
										return openBlock(), createBlock("tr", {
											key: role.id,
											class: "border-b last:border-0 hover:bg-muted/30 transition-colors"
										}, [
											createVNode("td", { class: "px-4 py-3 font-medium" }, toDisplayString(role.name), 1),
											createVNode("td", { class: "px-4 py-3 font-mono text-xs text-muted-foreground" }, toDisplayString(role.slug), 1),
											createVNode("td", { class: "px-4 py-3" }, [createVNode("span", { class: "inline-flex items-center gap-1 rounded-full bg-primary/10 text-primary px-2.5 py-0.5 text-xs font-medium" }, [createVNode(unref(ShieldCheck), { class: "h-3 w-3" }), createTextVNode(" " + toDisplayString(permCountFor(role)) + " permission" + toDisplayString(permCountFor(role) !== 1 ? "s" : ""), 1)])]),
											createVNode("td", { class: "px-4 py-3 text-right" }, [createVNode("div", { class: "flex justify-end gap-2" }, [unref(can)("role.update") ? (openBlock(), createBlock(_sfc_main$1, {
												key: 0,
												variant: "ghost",
												size: "icon",
												onClick: ($event) => openEdit(role)
											}, {
												default: withCtx(() => [createVNode(unref(Pencil), { class: "h-4 w-4" })]),
												_: 1
											}, 8, ["onClick"])) : createCommentVNode("", true), unref(can)("role.delete") ? (openBlock(), createBlock(_sfc_main$1, {
												key: 1,
												variant: "ghost",
												size: "icon",
												class: "text-destructive hover:text-destructive",
												onClick: ($event) => doDelete(role)
											}, {
												default: withCtx(() => [createVNode(unref(Trash2), { class: "h-4 w-4" })]),
												_: 1
											}, 8, ["onClick"])) : createCommentVNode("", true)])])
										]);
									}), 128))])])])), filteredRoles.value.length > 0 && !loading.value ? (openBlock(), createBlock(_sfc_main$9, {
										key: 3,
										page: page.value,
										"page-size": pageSize.value,
										total: filteredRoles.value.length,
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
							if (showDrawer.value) _push(`<div class="fixed inset-0 z-[50] bg-black/40 backdrop-blur-sm" data-v-4172b1b6${_scopeId}></div>`);
							else _push(`<!---->`);
							if (showDrawer.value) {
								_push(`<div class="fixed inset-y-0 right-0 z-[50] flex flex-col w-full sm:max-w-[500px] h-full bg-card shadow-2xl sm:border-l overflow-hidden" data-v-4172b1b6${_scopeId}><div class="flex items-center justify-between px-6 py-4 border-b shrink-0" data-v-4172b1b6${_scopeId}><div data-v-4172b1b6${_scopeId}><h3 class="font-semibold text-base" data-v-4172b1b6${_scopeId}>${ssrInterpolate(modalMode.value === "create" ? "Tambah Role" : "Edit Role")}</h3><p class="text-xs text-muted-foreground mt-0.5" data-v-4172b1b6${_scopeId}>${ssrInterpolate(modalMode.value === "create" ? "Isi detail role dan pilih permission." : "Perbarui informasi role dan permission.")}</p></div>`);
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
								_push(`</div><div class="flex-1 overflow-y-auto px-6 py-5 space-y-6" data-v-4172b1b6${_scopeId}>`);
								if (formError.value) _push(ssrRenderComponent(_sfc_main$6, { variant: "destructive" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`<p class="text-sm" data-v-4172b1b6${_scopeId}>${ssrInterpolate(formError.value)}</p>`);
										else return [createVNode("p", { class: "text-sm" }, toDisplayString(formError.value), 1)];
									}),
									_: 1
								}, _parent, _scopeId));
								else _push(`<!---->`);
								_push(`<div class="grid grid-cols-1 sm:grid-cols-2 gap-4" data-v-4172b1b6${_scopeId}><div class="space-y-1.5" data-v-4172b1b6${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, { for: "roleName" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Nama Role <span class="text-destructive" data-v-4172b1b6${_scopeId}>*</span>`);
										else return [createTextVNode("Nama Role "), createVNode("span", { class: "text-destructive" }, "*")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$5, {
									id: "roleName",
									modelValue: form.value.name,
									"onUpdate:modelValue": ($event) => form.value.name = $event,
									placeholder: "Contoh: Editor",
									onInput: onNameInput,
									disabled: saving.value
								}, null, _parent, _scopeId));
								_push(`</div><div class="space-y-1.5" data-v-4172b1b6${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, { for: "roleSlug" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Slug`);
										else return [createTextVNode("Slug")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$5, {
									id: "roleSlug",
									modelValue: form.value.slug,
									"onUpdate:modelValue": ($event) => form.value.slug = $event,
									placeholder: "contoh-role",
									class: "font-mono text-xs",
									disabled: saving.value
								}, null, _parent, _scopeId));
								_push(`</div></div><div class="space-y-3" data-v-4172b1b6${_scopeId}><div class="flex items-center justify-between" data-v-4172b1b6${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$7, { class: "text-sm font-semibold" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Hak Akses (Permissions)`);
										else return [createTextVNode("Hak Akses (Permissions)")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`<span class="text-[10px] text-muted-foreground bg-muted px-1.5 py-0.5 rounded" data-v-4172b1b6${_scopeId}>${ssrInterpolate(selectedPermIds.value.size)} dipilih </span></div>`);
								if (modules.value.length === 0) {
									_push(`<div class="flex items-center justify-center py-10 border rounded-lg bg-muted/20" data-v-4172b1b6${_scopeId}>`);
									_push(ssrRenderComponent(unref(Loader2), { class: "h-5 w-5 animate-spin text-muted-foreground/50 mr-2" }, null, _parent, _scopeId));
									_push(`<span class="text-sm text-muted-foreground" data-v-4172b1b6${_scopeId}>Memuat data hak akses…</span></div>`);
								} else {
									_push(`<div class="rounded-lg border border-zinc-200 dark:border-zinc-800 overflow-hidden" data-v-4172b1b6${_scopeId}><div class="overflow-x-auto" data-v-4172b1b6${_scopeId}><table class="w-full text-xs" data-v-4172b1b6${_scopeId}><thead data-v-4172b1b6${_scopeId}><tr class="bg-muted/50 border-b border-zinc-200 dark:border-zinc-800" data-v-4172b1b6${_scopeId}><th class="px-4 py-2.5 text-left font-semibold text-zinc-600 dark:text-zinc-400" data-v-4172b1b6${_scopeId}>Modul</th><!--[-->`);
									ssrRenderList(ACTIONS, (action) => {
										_push(`<th class="px-2 py-2.5 text-center font-semibold text-zinc-600 dark:text-zinc-400 capitalize" data-v-4172b1b6${_scopeId}>${ssrInterpolate(action)}</th>`);
									});
									_push(`<!--]--><th class="px-3 py-2.5 text-center font-semibold text-zinc-600 dark:text-zinc-400" data-v-4172b1b6${_scopeId}>Semua</th></tr></thead><tbody class="divide-y divide-zinc-100 dark:divide-zinc-800/60" data-v-4172b1b6${_scopeId}><!--[-->`);
									ssrRenderList(modules.value, (module) => {
										_push(`<tr class="hover:bg-zinc-50 dark:hover:bg-zinc-900/40 transition-colors" data-v-4172b1b6${_scopeId}><td class="px-4 py-3 font-medium capitalize text-zinc-900 dark:text-zinc-100" data-v-4172b1b6${_scopeId}>${ssrInterpolate(module)}</td><!--[-->`);
										ssrRenderList(ACTIONS, (action) => {
											_push(`<td class="px-2 py-4 text-center" data-v-4172b1b6${_scopeId}>`);
											if (getPermission(module, action)) {
												_push(`<div class="flex justify-center" data-v-4172b1b6${_scopeId}><button type="button" class="${ssrRenderClass([isChecked(module, action) ? "bg-primary/10 border-primary shadow-sm" : "bg-transparent border-zinc-200 dark:border-zinc-800 hover:border-zinc-400 dark:hover:border-zinc-600", "h-6 w-6 rounded-md flex items-center justify-center transition-all duration-200"])}" data-v-4172b1b6${_scopeId}><div class="${ssrRenderClass([isChecked(module, action) ? "border-primary bg-white dark:bg-zinc-900" : "border-zinc-300 dark:border-zinc-700", "h-4 w-4 rounded-sm border flex items-center justify-center transition-all"])}" data-v-4172b1b6${_scopeId}>`);
												if (isChecked(module, action)) _push(ssrRenderComponent(unref(Check), { class: "h-3 w-3 text-primary stroke-[4]" }, null, _parent, _scopeId));
												else _push(`<!---->`);
												_push(`</div></button></div>`);
											} else _push(`<div class="text-zinc-200 dark:text-zinc-800 select-none text-[10px]" data-v-4172b1b6${_scopeId}>—</div>`);
											_push(`</td>`);
										});
										_push(`<!--]--><td class="px-3 py-4 text-center bg-zinc-50/30 dark:bg-zinc-900/10" data-v-4172b1b6${_scopeId}><div class="flex justify-center" data-v-4172b1b6${_scopeId}><button type="button" class="${ssrRenderClass([isModuleAllChecked(module) ? "bg-primary/10 border-primary shadow-sm" : "bg-transparent border-zinc-200 dark:border-zinc-800 hover:border-zinc-400 dark:hover:border-zinc-600", "h-6 w-6 rounded-md flex items-center justify-center transition-all duration-200"])}" data-v-4172b1b6${_scopeId}><div class="${ssrRenderClass([isModuleAllChecked(module) ? "border-primary bg-white dark:bg-zinc-900" : "border-zinc-300 dark:border-zinc-700", "h-4 w-4 rounded-sm border flex items-center justify-center transition-all"])}" data-v-4172b1b6${_scopeId}>`);
										if (isModuleAllChecked(module)) _push(ssrRenderComponent(unref(Check), { class: "h-3 w-3 text-primary stroke-[4]" }, null, _parent, _scopeId));
										else _push(`<!---->`);
										_push(`</div></button></div></td></tr>`);
									});
									_push(`<!--]--></tbody></table></div></div>`);
								}
								_push(`</div></div><div class="flex justify-end gap-3 px-6 py-4 border-t shrink-0 bg-muted/30" data-v-4172b1b6${_scopeId}>`);
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
									onClick: saveRole,
									disabled: saving.value
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) {
											if (saving.value) _push(ssrRenderComponent(unref(Loader2), { class: "h-4 w-4 mr-2 animate-spin" }, null, _parent, _scopeId));
											else _push(`<!---->`);
											_push(` ${ssrInterpolate(modalMode.value === "create" ? "Simpan Role" : "Simpan Perubahan")}`);
										} else return [saving.value ? (openBlock(), createBlock(unref(Loader2), {
											key: 0,
											class: "h-4 w-4 mr-2 animate-spin"
										})) : createCommentVNode("", true), createTextVNode(" " + toDisplayString(modalMode.value === "create" ? "Simpan Role" : "Simpan Perubahan"), 1)];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`</div></div>`);
							} else _push(`<!---->`);
						}, "body", false, _parent);
					} else return [createVNode("div", { class: "pb-6" }, [
						createVNode("div", { class: "mb-6" }, [createVNode("h1", { class: "text-xl font-bold tracking-tight text-zinc-900" }, "Role Management"), createVNode("p", { class: "text-xs text-zinc-500 mt-0.5" }, " Manage system roles and their permissions. ")]),
						createVNode("div", { class: "flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-5" }, [createVNode(_sfc_main$8, {
							modelValue: searchQuery.value,
							"onUpdate:modelValue": ($event) => searchQuery.value = $event,
							placeholder: "Search roles...",
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
						}), unref(can)("role.store") ? (openBlock(), createBlock(_sfc_main$1, {
							key: 0,
							onClick: openCreate,
							size: "sm",
							class: "flex-1 sm:flex-none bg-primary hover:bg-primary/90 flex items-center justify-center gap-2"
						}, {
							default: withCtx(() => [createVNode(unref(Plus), { class: "h-4 w-4" }), createVNode("span", null, "Add Role")]),
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
								}, [createVNode(unref(Loader2), { class: "h-6 w-6 animate-spin text-muted-foreground" })])) : filteredRoles.value.length === 0 ? (openBlock(), createBlock("div", {
									key: 1,
									class: "flex flex-col items-center justify-center py-20 text-muted-foreground"
								}, [createVNode(unref(ShieldCheck), { class: "h-10 w-10 mb-3 opacity-40" }), createVNode("p", { class: "text-sm" }, "No roles yet.")])) : (openBlock(), createBlock("div", {
									key: 2,
									class: "overflow-x-auto"
								}, [createVNode("table", { class: "w-full text-sm" }, [createVNode("thead", null, [createVNode("tr", { class: "border-b bg-muted/40" }, [
									createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Name"),
									createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Slug"),
									createVNode("th", { class: "px-4 py-3 text-left font-medium text-muted-foreground" }, "Permissions"),
									createVNode("th", { class: "px-4 py-3 text-right font-medium text-muted-foreground" }, "Actions")
								])]), createVNode("tbody", null, [(openBlock(true), createBlock(Fragment, null, renderList(paginatedRoles.value, (role) => {
									return openBlock(), createBlock("tr", {
										key: role.id,
										class: "border-b last:border-0 hover:bg-muted/30 transition-colors"
									}, [
										createVNode("td", { class: "px-4 py-3 font-medium" }, toDisplayString(role.name), 1),
										createVNode("td", { class: "px-4 py-3 font-mono text-xs text-muted-foreground" }, toDisplayString(role.slug), 1),
										createVNode("td", { class: "px-4 py-3" }, [createVNode("span", { class: "inline-flex items-center gap-1 rounded-full bg-primary/10 text-primary px-2.5 py-0.5 text-xs font-medium" }, [createVNode(unref(ShieldCheck), { class: "h-3 w-3" }), createTextVNode(" " + toDisplayString(permCountFor(role)) + " permission" + toDisplayString(permCountFor(role) !== 1 ? "s" : ""), 1)])]),
										createVNode("td", { class: "px-4 py-3 text-right" }, [createVNode("div", { class: "flex justify-end gap-2" }, [unref(can)("role.update") ? (openBlock(), createBlock(_sfc_main$1, {
											key: 0,
											variant: "ghost",
											size: "icon",
											onClick: ($event) => openEdit(role)
										}, {
											default: withCtx(() => [createVNode(unref(Pencil), { class: "h-4 w-4" })]),
											_: 1
										}, 8, ["onClick"])) : createCommentVNode("", true), unref(can)("role.delete") ? (openBlock(), createBlock(_sfc_main$1, {
											key: 1,
											variant: "ghost",
											size: "icon",
											class: "text-destructive hover:text-destructive",
											onClick: ($event) => doDelete(role)
										}, {
											default: withCtx(() => [createVNode(unref(Trash2), { class: "h-4 w-4" })]),
											_: 1
										}, 8, ["onClick"])) : createCommentVNode("", true)])])
									]);
								}), 128))])])])), filteredRoles.value.length > 0 && !loading.value ? (openBlock(), createBlock(_sfc_main$9, {
									key: 3,
									page: page.value,
									"page-size": pageSize.value,
									total: filteredRoles.value.length,
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
							class: "fixed inset-y-0 right-0 z-[50] flex flex-col w-full sm:max-w-[500px] h-full bg-card shadow-2xl sm:border-l overflow-hidden"
						}, [
							createVNode("div", { class: "flex items-center justify-between px-6 py-4 border-b shrink-0" }, [createVNode("div", null, [createVNode("h3", { class: "font-semibold text-base" }, toDisplayString(modalMode.value === "create" ? "Tambah Role" : "Edit Role"), 1), createVNode("p", { class: "text-xs text-muted-foreground mt-0.5" }, toDisplayString(modalMode.value === "create" ? "Isi detail role dan pilih permission." : "Perbarui informasi role dan permission."), 1)]), createVNode(_sfc_main$1, {
								variant: "ghost",
								size: "icon",
								onClick: closeDrawer
							}, {
								default: withCtx(() => [createVNode(unref(X), { class: "h-4 w-4" })]),
								_: 1
							})]),
							createVNode("div", { class: "flex-1 overflow-y-auto px-6 py-5 space-y-6" }, [
								formError.value ? (openBlock(), createBlock(_sfc_main$6, {
									key: 0,
									variant: "destructive"
								}, {
									default: withCtx(() => [createVNode("p", { class: "text-sm" }, toDisplayString(formError.value), 1)]),
									_: 1
								})) : createCommentVNode("", true),
								createVNode("div", { class: "grid grid-cols-1 sm:grid-cols-2 gap-4" }, [createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, { for: "roleName" }, {
									default: withCtx(() => [createTextVNode("Nama Role "), createVNode("span", { class: "text-destructive" }, "*")]),
									_: 1
								}), createVNode(_sfc_main$5, {
									id: "roleName",
									modelValue: form.value.name,
									"onUpdate:modelValue": ($event) => form.value.name = $event,
									placeholder: "Contoh: Editor",
									onInput: onNameInput,
									disabled: saving.value
								}, null, 8, [
									"modelValue",
									"onUpdate:modelValue",
									"disabled"
								])]), createVNode("div", { class: "space-y-1.5" }, [createVNode(_sfc_main$7, { for: "roleSlug" }, {
									default: withCtx(() => [createTextVNode("Slug")]),
									_: 1
								}), createVNode(_sfc_main$5, {
									id: "roleSlug",
									modelValue: form.value.slug,
									"onUpdate:modelValue": ($event) => form.value.slug = $event,
									placeholder: "contoh-role",
									class: "font-mono text-xs",
									disabled: saving.value
								}, null, 8, [
									"modelValue",
									"onUpdate:modelValue",
									"disabled"
								])])]),
								createVNode("div", { class: "space-y-3" }, [createVNode("div", { class: "flex items-center justify-between" }, [createVNode(_sfc_main$7, { class: "text-sm font-semibold" }, {
									default: withCtx(() => [createTextVNode("Hak Akses (Permissions)")]),
									_: 1
								}), createVNode("span", { class: "text-[10px] text-muted-foreground bg-muted px-1.5 py-0.5 rounded" }, toDisplayString(selectedPermIds.value.size) + " dipilih ", 1)]), modules.value.length === 0 ? (openBlock(), createBlock("div", {
									key: 0,
									class: "flex items-center justify-center py-10 border rounded-lg bg-muted/20"
								}, [createVNode(unref(Loader2), { class: "h-5 w-5 animate-spin text-muted-foreground/50 mr-2" }), createVNode("span", { class: "text-sm text-muted-foreground" }, "Memuat data hak akses…")])) : (openBlock(), createBlock("div", {
									key: 1,
									class: "rounded-lg border border-zinc-200 dark:border-zinc-800 overflow-hidden"
								}, [createVNode("div", { class: "overflow-x-auto" }, [createVNode("table", { class: "w-full text-xs" }, [createVNode("thead", null, [createVNode("tr", { class: "bg-muted/50 border-b border-zinc-200 dark:border-zinc-800" }, [
									createVNode("th", { class: "px-4 py-2.5 text-left font-semibold text-zinc-600 dark:text-zinc-400" }, "Modul"),
									(openBlock(), createBlock(Fragment, null, renderList(ACTIONS, (action) => {
										return createVNode("th", {
											class: "px-2 py-2.5 text-center font-semibold text-zinc-600 dark:text-zinc-400 capitalize",
											key: action
										}, toDisplayString(action), 1);
									}), 64)),
									createVNode("th", { class: "px-3 py-2.5 text-center font-semibold text-zinc-600 dark:text-zinc-400" }, "Semua")
								])]), createVNode("tbody", { class: "divide-y divide-zinc-100 dark:divide-zinc-800/60" }, [(openBlock(true), createBlock(Fragment, null, renderList(modules.value, (module) => {
									return openBlock(), createBlock("tr", {
										key: module,
										class: "hover:bg-zinc-50 dark:hover:bg-zinc-900/40 transition-colors"
									}, [
										createVNode("td", { class: "px-4 py-3 font-medium capitalize text-zinc-900 dark:text-zinc-100" }, toDisplayString(module), 1),
										(openBlock(), createBlock(Fragment, null, renderList(ACTIONS, (action) => {
											return createVNode("td", {
												key: action,
												class: "px-2 py-4 text-center"
											}, [getPermission(module, action) ? (openBlock(), createBlock("div", {
												key: 0,
												class: "flex justify-center"
											}, [createVNode("button", {
												type: "button",
												onClick: ($event) => togglePerm(module, action),
												class: ["h-6 w-6 rounded-md flex items-center justify-center transition-all duration-200", isChecked(module, action) ? "bg-primary/10 border-primary shadow-sm" : "bg-transparent border-zinc-200 dark:border-zinc-800 hover:border-zinc-400 dark:hover:border-zinc-600"]
											}, [createVNode("div", { class: ["h-4 w-4 rounded-sm border flex items-center justify-center transition-all", isChecked(module, action) ? "border-primary bg-white dark:bg-zinc-900" : "border-zinc-300 dark:border-zinc-700"] }, [isChecked(module, action) ? (openBlock(), createBlock(unref(Check), {
												key: 0,
												class: "h-3 w-3 text-primary stroke-[4]"
											})) : createCommentVNode("", true)], 2)], 10, ["onClick"])])) : (openBlock(), createBlock("div", {
												key: 1,
												class: "text-zinc-200 dark:text-zinc-800 select-none text-[10px]"
											}, "—"))]);
										}), 64)),
										createVNode("td", { class: "px-3 py-4 text-center bg-zinc-50/30 dark:bg-zinc-900/10" }, [createVNode("div", { class: "flex justify-center" }, [createVNode("button", {
											type: "button",
											onClick: ($event) => toggleAllInModule(module),
											class: ["h-6 w-6 rounded-md flex items-center justify-center transition-all duration-200", isModuleAllChecked(module) ? "bg-primary/10 border-primary shadow-sm" : "bg-transparent border-zinc-200 dark:border-zinc-800 hover:border-zinc-400 dark:hover:border-zinc-600"]
										}, [createVNode("div", { class: ["h-4 w-4 rounded-sm border flex items-center justify-center transition-all", isModuleAllChecked(module) ? "border-primary bg-white dark:bg-zinc-900" : "border-zinc-300 dark:border-zinc-700"] }, [isModuleAllChecked(module) ? (openBlock(), createBlock(unref(Check), {
											key: 0,
											class: "h-3 w-3 text-primary stroke-[4]"
										})) : createCommentVNode("", true)], 2)], 10, ["onClick"])])])
									]);
								}), 128))])])])]))])
							]),
							createVNode("div", { class: "flex justify-end gap-3 px-6 py-4 border-t shrink-0 bg-muted/30" }, [createVNode(_sfc_main$1, {
								variant: "outline",
								onClick: closeDrawer,
								disabled: saving.value
							}, {
								default: withCtx(() => [createTextVNode("Batal")]),
								_: 1
							}, 8, ["disabled"]), createVNode(_sfc_main$1, {
								onClick: saveRole,
								disabled: saving.value
							}, {
								default: withCtx(() => [saving.value ? (openBlock(), createBlock(unref(Loader2), {
									key: 0,
									class: "h-4 w-4 mr-2 animate-spin"
								})) : createCommentVNode("", true), createTextVNode(" " + toDisplayString(modalMode.value === "create" ? "Simpan Role" : "Simpan Perubahan"), 1)]),
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
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/pages/RolesPage.vue");
	return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
var RolesPage_default = /* @__PURE__ */ _plugin_vue_export_helper_default(_sfc_main, [["__scopeId", "data-v-4172b1b6"]]);
//#endregion
export { RolesPage_default as default };
