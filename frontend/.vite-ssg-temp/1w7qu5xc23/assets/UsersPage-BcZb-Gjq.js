import { r as api } from "../main.mjs";
import { t as _plugin_vue_export_helper_default } from "./_plugin-vue_export-helper-DMwexRDj.js";
import "./Button-Bj0EF1Kv.js";
import { a as _sfc_main$6, d as usePermission, i as _sfc_main$7, n as _sfc_main$5, o as _sfc_main$4, r as _sfc_main$8, s as _sfc_main$3, t as _sfc_main$2 } from "./AppLayout-D1IhsFmL.js";
import { t as useToast } from "./useToast-BeMK7Zjj.js";
import { t as _sfc_main$9 } from "./Card-ClMbbMGU.js";
import { t as _sfc_main$10 } from "./CardContent-g9O7qVnh.js";
import { t as _sfc_main$11 } from "./DataTableSearch-DI2aluk6.js";
import { t as _sfc_main$12 } from "./DataTablePagination-CRAPEico.js";
import { a as _sfc_main$14, i as _sfc_main$17, n as _sfc_main$16, o as _sfc_main$13, r as _sfc_main$15, t as _sfc_main$18 } from "./TableCell-DkUauQ5R.js";
import { Fragment, Teleport, Transition, computed, createBlock, createCommentVNode, createTextVNode, createVNode, mergeProps, onBeforeUnmount, onMounted, openBlock, ref, renderList, renderSlot, toDisplayString, unref, useSSRContext, vModelDynamic, vModelText, watch, withCtx, withDirectives } from "vue";
import { ssrIncludeBooleanAttr, ssrInterpolate, ssrRenderAttr, ssrRenderAttrs, ssrRenderClass, ssrRenderComponent, ssrRenderDynamicModel, ssrRenderList, ssrRenderSlot, ssrRenderStyle, ssrRenderTeleport } from "vue/server-renderer";
import { ArrowUpDown, Check, ChevronDown, Eye, EyeOff, MoreVertical, Pencil, Plus, Shield, Trash2, X } from "lucide-vue-next";
//#region src/components/ui/DataTable.vue
var _sfc_main$1 = {
	__name: "DataTable",
	__ssrInlineRender: true,
	props: {
		data: {
			type: Array,
			required: true
		},
		columns: {
			type: Array,
			required: true
		},
		page: {
			type: Number,
			default: 1
		},
		pageSize: {
			type: Number,
			default: 10
		},
		total: {
			type: Number,
			default: 0
		},
		loading: {
			type: Boolean,
			default: false
		},
		emptyMessage: {
			type: String,
			default: "No data available."
		},
		sortable: {
			type: Boolean,
			default: true
		},
		selectable: {
			type: Boolean,
			default: false
		},
		rowKey: {
			type: String,
			default: "id"
		}
	},
	emits: [
		"update:page",
		"update:pageSize",
		"sort",
		"selection-change"
	],
	setup(__props, { emit: __emit }) {
		const props = __props;
		const emit = __emit;
		computed(() => Math.ceil(props.total / props.pageSize));
		computed(() => (props.page - 1) * props.pageSize + 1);
		computed(() => Math.min(props.page * props.pageSize, props.total));
		function handleSort(column) {
			if (!props.sortable || !column.sortable) return;
			emit("sort", {
				field: column.key,
				order: column.sortOrder === "asc" ? "desc" : "asc"
			});
		}
		return (_ctx, _push, _parent, _attrs) => {
			_push(`<div${ssrRenderAttrs(mergeProps({ class: "flex flex-col" }, _attrs))}><div class="bg-card">`);
			if (__props.loading) _push(`<div class="flex items-center justify-center py-20"><div class="flex flex-col items-center gap-3"><div class="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div><p class="text-sm text-muted-foreground">Loading data...</p></div></div>`);
			else {
				_push(`<!--[-->`);
				if (__props.data.length === 0) {
					_push(`<div class="flex flex-col items-center justify-center py-20 px-6"><div class="rounded-full bg-muted p-4 mb-3">`);
					ssrRenderSlot(_ctx.$slots, "empty-icon", {}, () => {
						_push(`<div class="h-8 w-8 text-muted-foreground"></div>`);
					}, _push, _parent);
					_push(`</div><p class="text-sm text-muted-foreground">${ssrInterpolate(__props.emptyMessage)}</p>`);
					ssrRenderSlot(_ctx.$slots, "empty-action", {}, null, _push, _parent);
					_push(`</div>`);
				} else {
					_push(`<div class="overflow-x-auto">`);
					_push(ssrRenderComponent(_sfc_main$13, null, {
						default: withCtx((_, _push, _parent, _scopeId) => {
							if (_push) {
								_push(ssrRenderComponent(_sfc_main$14, null, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(ssrRenderComponent(_sfc_main$15, { class: "hover:bg-transparent" }, {
											default: withCtx((_, _push, _parent, _scopeId) => {
												if (_push) {
													if (__props.selectable) _push(ssrRenderComponent(_sfc_main$16, { class: "w-12" }, {
														default: withCtx((_, _push, _parent, _scopeId) => {
															if (_push) ssrRenderSlot(_ctx.$slots, "select-all", {}, null, _push, _parent, _scopeId);
															else return [renderSlot(_ctx.$slots, "select-all")];
														}),
														_: 3
													}, _parent, _scopeId));
													else _push(`<!---->`);
													_push(`<!--[-->`);
													ssrRenderList(__props.columns, (column) => {
														_push(ssrRenderComponent(_sfc_main$16, {
															style: {
																width: column.width,
																minWidth: column.minWidth
															},
															class: { "cursor-pointer select-none hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors": __props.sortable && column.sortable },
															onClick: ($event) => handleSort(column)
														}, {
															default: withCtx((_, _push, _parent, _scopeId) => {
																if (_push) {
																	_push(`<div class="flex items-center gap-2"${_scopeId}><span${_scopeId}>${ssrInterpolate(column.label)}</span>`);
																	if (__props.sortable && column.sortable) _push(ssrRenderComponent(unref(ArrowUpDown), { class: ["h-3.5 w-3.5 shrink-0 transition-opacity", column.sortOrder ? "opacity-100" : "opacity-20"] }, null, _parent, _scopeId));
																	else _push(`<!---->`);
																	_push(`</div>`);
																} else return [createVNode("div", { class: "flex items-center gap-2" }, [createVNode("span", null, toDisplayString(column.label), 1), __props.sortable && column.sortable ? (openBlock(), createBlock(unref(ArrowUpDown), {
																	key: 0,
																	class: ["h-3.5 w-3.5 shrink-0 transition-opacity", column.sortOrder ? "opacity-100" : "opacity-20"]
																}, null, 8, ["class"])) : createCommentVNode("", true)])];
															}),
															_: 2
														}, _parent, _scopeId));
													});
													_push(`<!--]-->`);
													if (_ctx.$slots.actions) _push(ssrRenderComponent(_sfc_main$16, { class: "w-24 text-right pr-6" }, {
														default: withCtx((_, _push, _parent, _scopeId) => {
															if (_push) _push(`Actions`);
															else return [createTextVNode("Actions")];
														}),
														_: 1
													}, _parent, _scopeId));
													else _push(`<!---->`);
												} else return [
													__props.selectable ? (openBlock(), createBlock(_sfc_main$16, {
														key: 0,
														class: "w-12"
													}, {
														default: withCtx(() => [renderSlot(_ctx.$slots, "select-all")]),
														_: 3
													})) : createCommentVNode("", true),
													(openBlock(true), createBlock(Fragment, null, renderList(__props.columns, (column) => {
														return openBlock(), createBlock(_sfc_main$16, {
															key: column.key,
															style: {
																width: column.width,
																minWidth: column.minWidth
															},
															class: { "cursor-pointer select-none hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors": __props.sortable && column.sortable },
															onClick: ($event) => handleSort(column)
														}, {
															default: withCtx(() => [createVNode("div", { class: "flex items-center gap-2" }, [createVNode("span", null, toDisplayString(column.label), 1), __props.sortable && column.sortable ? (openBlock(), createBlock(unref(ArrowUpDown), {
																key: 0,
																class: ["h-3.5 w-3.5 shrink-0 transition-opacity", column.sortOrder ? "opacity-100" : "opacity-20"]
															}, null, 8, ["class"])) : createCommentVNode("", true)])]),
															_: 2
														}, 1032, [
															"style",
															"class",
															"onClick"
														]);
													}), 128)),
													_ctx.$slots.actions ? (openBlock(), createBlock(_sfc_main$16, {
														key: 1,
														class: "w-24 text-right pr-6"
													}, {
														default: withCtx(() => [createTextVNode("Actions")]),
														_: 1
													})) : createCommentVNode("", true)
												];
											}),
											_: 3
										}, _parent, _scopeId));
										else return [createVNode(_sfc_main$15, { class: "hover:bg-transparent" }, {
											default: withCtx(() => [
												__props.selectable ? (openBlock(), createBlock(_sfc_main$16, {
													key: 0,
													class: "w-12"
												}, {
													default: withCtx(() => [renderSlot(_ctx.$slots, "select-all")]),
													_: 3
												})) : createCommentVNode("", true),
												(openBlock(true), createBlock(Fragment, null, renderList(__props.columns, (column) => {
													return openBlock(), createBlock(_sfc_main$16, {
														key: column.key,
														style: {
															width: column.width,
															minWidth: column.minWidth
														},
														class: { "cursor-pointer select-none hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors": __props.sortable && column.sortable },
														onClick: ($event) => handleSort(column)
													}, {
														default: withCtx(() => [createVNode("div", { class: "flex items-center gap-2" }, [createVNode("span", null, toDisplayString(column.label), 1), __props.sortable && column.sortable ? (openBlock(), createBlock(unref(ArrowUpDown), {
															key: 0,
															class: ["h-3.5 w-3.5 shrink-0 transition-opacity", column.sortOrder ? "opacity-100" : "opacity-20"]
														}, null, 8, ["class"])) : createCommentVNode("", true)])]),
														_: 2
													}, 1032, [
														"style",
														"class",
														"onClick"
													]);
												}), 128)),
												_ctx.$slots.actions ? (openBlock(), createBlock(_sfc_main$16, {
													key: 1,
													class: "w-24 text-right pr-6"
												}, {
													default: withCtx(() => [createTextVNode("Actions")]),
													_: 1
												})) : createCommentVNode("", true)
											]),
											_: 3
										})];
									}),
									_: 3
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$17, null, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) {
											_push(`<!--[-->`);
											ssrRenderList(__props.data, (item, index) => {
												_push(ssrRenderComponent(_sfc_main$15, {
													key: item[__props.rowKey] || index,
													class: "group"
												}, {
													default: withCtx((_, _push, _parent, _scopeId) => {
														if (_push) {
															if (__props.selectable) _push(ssrRenderComponent(_sfc_main$18, null, {
																default: withCtx((_, _push, _parent, _scopeId) => {
																	if (_push) ssrRenderSlot(_ctx.$slots, "select", { item }, null, _push, _parent, _scopeId);
																	else return [renderSlot(_ctx.$slots, "select", { item })];
																}),
																_: 2
															}, _parent, _scopeId));
															else _push(`<!---->`);
															_push(`<!--[-->`);
															ssrRenderList(__props.columns, (column) => {
																_push(ssrRenderComponent(_sfc_main$18, { class: "align-middle whitespace-nowrap" }, {
																	default: withCtx((_, _push, _parent, _scopeId) => {
																		if (_push) ssrRenderSlot(_ctx.$slots, `cell-${column.key}`, {
																			item,
																			value: item[column.key],
																			index
																		}, () => {
																			_push(`<span class="text-zinc-600 dark:text-zinc-400 font-medium"${_scopeId}>${ssrInterpolate(item[column.key])}</span>`);
																		}, _push, _parent, _scopeId);
																		else return [renderSlot(_ctx.$slots, `cell-${column.key}`, {
																			item,
																			value: item[column.key],
																			index
																		}, () => [createVNode("span", { class: "text-zinc-600 dark:text-zinc-400 font-medium" }, toDisplayString(item[column.key]), 1)])];
																	}),
																	_: 2
																}, _parent, _scopeId));
															});
															_push(`<!--]-->`);
															if (_ctx.$slots.actions) _push(ssrRenderComponent(_sfc_main$18, { class: "text-right" }, {
																default: withCtx((_, _push, _parent, _scopeId) => {
																	if (_push) ssrRenderSlot(_ctx.$slots, "actions", { item }, null, _push, _parent, _scopeId);
																	else return [renderSlot(_ctx.$slots, "actions", { item })];
																}),
																_: 2
															}, _parent, _scopeId));
															else _push(`<!---->`);
														} else return [
															__props.selectable ? (openBlock(), createBlock(_sfc_main$18, { key: 0 }, {
																default: withCtx(() => [renderSlot(_ctx.$slots, "select", { item })]),
																_: 2
															}, 1024)) : createCommentVNode("", true),
															(openBlock(true), createBlock(Fragment, null, renderList(__props.columns, (column) => {
																return openBlock(), createBlock(_sfc_main$18, {
																	key: column.key,
																	class: "align-middle whitespace-nowrap"
																}, {
																	default: withCtx(() => [renderSlot(_ctx.$slots, `cell-${column.key}`, {
																		item,
																		value: item[column.key],
																		index
																	}, () => [createVNode("span", { class: "text-zinc-600 dark:text-zinc-400 font-medium" }, toDisplayString(item[column.key]), 1)])]),
																	_: 2
																}, 1024);
															}), 128)),
															_ctx.$slots.actions ? (openBlock(), createBlock(_sfc_main$18, {
																key: 1,
																class: "text-right"
															}, {
																default: withCtx(() => [renderSlot(_ctx.$slots, "actions", { item })]),
																_: 2
															}, 1024)) : createCommentVNode("", true)
														];
													}),
													_: 2
												}, _parent, _scopeId));
											});
											_push(`<!--]-->`);
										} else return [(openBlock(true), createBlock(Fragment, null, renderList(__props.data, (item, index) => {
											return openBlock(), createBlock(_sfc_main$15, {
												key: item[__props.rowKey] || index,
												class: "group"
											}, {
												default: withCtx(() => [
													__props.selectable ? (openBlock(), createBlock(_sfc_main$18, { key: 0 }, {
														default: withCtx(() => [renderSlot(_ctx.$slots, "select", { item })]),
														_: 2
													}, 1024)) : createCommentVNode("", true),
													(openBlock(true), createBlock(Fragment, null, renderList(__props.columns, (column) => {
														return openBlock(), createBlock(_sfc_main$18, {
															key: column.key,
															class: "align-middle whitespace-nowrap"
														}, {
															default: withCtx(() => [renderSlot(_ctx.$slots, `cell-${column.key}`, {
																item,
																value: item[column.key],
																index
															}, () => [createVNode("span", { class: "text-zinc-600 dark:text-zinc-400 font-medium" }, toDisplayString(item[column.key]), 1)])]),
															_: 2
														}, 1024);
													}), 128)),
													_ctx.$slots.actions ? (openBlock(), createBlock(_sfc_main$18, {
														key: 1,
														class: "text-right"
													}, {
														default: withCtx(() => [renderSlot(_ctx.$slots, "actions", { item })]),
														_: 2
													}, 1024)) : createCommentVNode("", true)
												]),
												_: 2
											}, 1024);
										}), 128))];
									}),
									_: 3
								}, _parent, _scopeId));
							} else return [createVNode(_sfc_main$14, null, {
								default: withCtx(() => [createVNode(_sfc_main$15, { class: "hover:bg-transparent" }, {
									default: withCtx(() => [
										__props.selectable ? (openBlock(), createBlock(_sfc_main$16, {
											key: 0,
											class: "w-12"
										}, {
											default: withCtx(() => [renderSlot(_ctx.$slots, "select-all")]),
											_: 3
										})) : createCommentVNode("", true),
										(openBlock(true), createBlock(Fragment, null, renderList(__props.columns, (column) => {
											return openBlock(), createBlock(_sfc_main$16, {
												key: column.key,
												style: {
													width: column.width,
													minWidth: column.minWidth
												},
												class: { "cursor-pointer select-none hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors": __props.sortable && column.sortable },
												onClick: ($event) => handleSort(column)
											}, {
												default: withCtx(() => [createVNode("div", { class: "flex items-center gap-2" }, [createVNode("span", null, toDisplayString(column.label), 1), __props.sortable && column.sortable ? (openBlock(), createBlock(unref(ArrowUpDown), {
													key: 0,
													class: ["h-3.5 w-3.5 shrink-0 transition-opacity", column.sortOrder ? "opacity-100" : "opacity-20"]
												}, null, 8, ["class"])) : createCommentVNode("", true)])]),
												_: 2
											}, 1032, [
												"style",
												"class",
												"onClick"
											]);
										}), 128)),
										_ctx.$slots.actions ? (openBlock(), createBlock(_sfc_main$16, {
											key: 1,
											class: "w-24 text-right pr-6"
										}, {
											default: withCtx(() => [createTextVNode("Actions")]),
											_: 1
										})) : createCommentVNode("", true)
									]),
									_: 3
								})]),
								_: 3
							}), createVNode(_sfc_main$17, null, {
								default: withCtx(() => [(openBlock(true), createBlock(Fragment, null, renderList(__props.data, (item, index) => {
									return openBlock(), createBlock(_sfc_main$15, {
										key: item[__props.rowKey] || index,
										class: "group"
									}, {
										default: withCtx(() => [
											__props.selectable ? (openBlock(), createBlock(_sfc_main$18, { key: 0 }, {
												default: withCtx(() => [renderSlot(_ctx.$slots, "select", { item })]),
												_: 2
											}, 1024)) : createCommentVNode("", true),
											(openBlock(true), createBlock(Fragment, null, renderList(__props.columns, (column) => {
												return openBlock(), createBlock(_sfc_main$18, {
													key: column.key,
													class: "align-middle whitespace-nowrap"
												}, {
													default: withCtx(() => [renderSlot(_ctx.$slots, `cell-${column.key}`, {
														item,
														value: item[column.key],
														index
													}, () => [createVNode("span", { class: "text-zinc-600 dark:text-zinc-400 font-medium" }, toDisplayString(item[column.key]), 1)])]),
													_: 2
												}, 1024);
											}), 128)),
											_ctx.$slots.actions ? (openBlock(), createBlock(_sfc_main$18, {
												key: 1,
												class: "text-right"
											}, {
												default: withCtx(() => [renderSlot(_ctx.$slots, "actions", { item })]),
												_: 2
											}, 1024)) : createCommentVNode("", true)
										]),
										_: 2
									}, 1024);
								}), 128))]),
								_: 3
							})];
						}),
						_: 3
					}, _parent));
					_push(`</div>`);
				}
				_push(`<!--]-->`);
			}
			_push(`</div>`);
			if (__props.data.length > 0 && !__props.loading) _push(ssrRenderComponent(_sfc_main$12, {
				page: __props.page,
				"page-size": __props.pageSize,
				total: __props.total,
				"onUpdate:page": ($event) => emit("update:page", $event),
				"onUpdate:pageSize": ($event) => emit("update:pageSize", $event)
			}, null, _parent));
			else _push(`<!---->`);
			_push(`</div>`);
		};
	}
};
var _sfc_setup$1 = _sfc_main$1.setup;
_sfc_main$1.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/DataTable.vue");
	return _sfc_setup$1 ? _sfc_setup$1(props, ctx) : void 0;
};
//#endregion
//#region src/pages/UsersPage.vue
var _sfc_main = {
	__name: "UsersPage",
	__ssrInlineRender: true,
	setup(__props) {
		const { can } = usePermission();
		const { toast } = useToast();
		const allUsers = ref([]);
		const roles = ref([]);
		const loading = ref(false);
		const searchQuery = ref("");
		const page = ref(1);
		const pageSize = ref(10);
		const showDrawer = ref(false);
		const drawerMode = ref("create");
		const saving = ref(false);
		const formError = ref(null);
		const showPassword = ref(false);
		const form = ref({
			id: null,
			username: "",
			fullname: "",
			email: "",
			password: "",
			phone: "",
			roleIds: []
		});
		const fieldErrors = ref({
			username: "",
			email: "",
			phone: "",
			password: ""
		});
		function clearFieldErrors() {
			fieldErrors.value = {
				username: "",
				email: "",
				phone: "",
				password: ""
			};
		}
		function validateForm() {
			clearFieldErrors();
			let valid = true;
			const uname = form.value.username.trim();
			if (!uname) {
				fieldErrors.value.username = "Username is required.";
				valid = false;
			} else if (uname.length < 3) {
				fieldErrors.value.username = "Username must be at least 3 characters.";
				valid = false;
			} else if (uname.length > 30) {
				fieldErrors.value.username = "Username must not exceed 30 characters.";
				valid = false;
			} else if (!/^[a-zA-Z0-9_]+$/.test(uname)) {
				fieldErrors.value.username = "Username may only contain letters, numbers, and underscores.";
				valid = false;
			}
			const email = form.value.email.trim();
			if (email) {
				if (!email.includes("@") || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
					fieldErrors.value.email = "Please enter a valid email address (e.g. john@example.com).";
					valid = false;
				}
			}
			const phone = form.value.phone.trim();
			if (phone) {
				if (!/^\d+$/.test(phone)) {
					fieldErrors.value.phone = "Phone number may only contain digits.";
					valid = false;
				} else if (phone.length < 8) {
					fieldErrors.value.phone = "Phone number must be at least 8 digits.";
					valid = false;
				} else if (phone.length > 15) {
					fieldErrors.value.phone = "Phone number must not exceed 15 digits.";
					valid = false;
				}
			}
			const pwd = form.value.password;
			if (drawerMode.value === "create" && !pwd) {
				fieldErrors.value.password = "Password is required.";
				valid = false;
			} else if (pwd && pwd.length < 6) {
				fieldErrors.value.password = "Password must be at least 6 characters.";
				valid = false;
			}
			return valid;
		}
		const avatarPreview = ref(null);
		const avatarFile = ref(null);
		const avatarInputRef = ref(null);
		function handleAvatarChange(e) {
			const file = e.target.files?.[0];
			if (!file) return;
			avatarFile.value = file;
			const reader = new FileReader();
			reader.onload = (ev) => {
				avatarPreview.value = ev.target.result;
			};
			reader.readAsDataURL(file);
		}
		function removeAvatar() {
			avatarPreview.value = null;
			avatarFile.value = null;
			if (avatarInputRef.value) avatarInputRef.value.value = "";
		}
		const showDeleteDialog = ref(false);
		const deletingUser = ref(null);
		const deleteAdminUsername = ref("");
		const deleteAdminPassword = ref("");
		const showDeletePassword = ref(false);
		const deleteError = ref("");
		const columns = computed(() => [
			{
				key: "username",
				label: "User",
				sortable: true
			},
			{
				key: "roles",
				label: "Role"
			},
			{
				key: "phone",
				label: "Phone"
			},
			{
				key: "createdAt",
				label: "Joined",
				sortable: true
			}
		]);
		const filterOpen = ref(false);
		const activeFilters = ref({
			roles: [],
			hasPhone: null
		});
		const availableRoles = computed(() => {
			const map = {};
			allUsers.value.forEach((u) => {
				(u.roles || []).forEach((r) => {
					if (r.slug) map[r.slug] = r.name;
				});
			});
			return Object.entries(map).map(([slug, name]) => ({
				slug,
				name
			}));
		});
		const activeFilterCount = computed(() => {
			let c = activeFilters.value.roles.length;
			if (activeFilters.value.hasPhone !== null) c++;
			return c;
		});
		function toggleRoleFilter(slug) {
			const idx = activeFilters.value.roles.indexOf(slug);
			if (idx === -1) activeFilters.value.roles.push(slug);
			else activeFilters.value.roles.splice(idx, 1);
			page.value = 1;
		}
		function setPhoneFilter(val) {
			activeFilters.value.hasPhone = activeFilters.value.hasPhone === val ? null : val;
			page.value = 1;
		}
		function clearFilters() {
			activeFilters.value = {
				roles: [],
				hasPhone: null
			};
			page.value = 1;
		}
		const filteredUsers = computed(() => {
			let list = allUsers.value;
			const q = searchQuery.value.trim().toLowerCase();
			if (q) list = list.filter((u) => (u.fullname || "").toLowerCase().includes(q) || (u.username || "").toLowerCase().includes(q) || (u.phone || "").includes(q) || (u.roles || []).some((r) => r.name.toLowerCase().includes(q)));
			if (activeFilters.value.roles.length > 0) list = list.filter((u) => (u.roles || []).some((r) => activeFilters.value.roles.includes(r.slug)));
			if (activeFilters.value.hasPhone === true) list = list.filter((u) => u.phone && u.phone.trim());
			else if (activeFilters.value.hasPhone === false) list = list.filter((u) => !u.phone || !u.phone.trim());
			return list;
		});
		const pagedUsers = computed(() => {
			const start = (page.value - 1) * pageSize.value;
			return filteredUsers.value.slice(start, start + pageSize.value);
		});
		const total = computed(() => filteredUsers.value.length);
		watch(searchQuery, () => {
			page.value = 1;
		});
		watch(pageSize, () => {
			page.value = 1;
		});
		watch(activeFilters, () => {
			page.value = 1;
		}, { deep: true });
		async function fetchUsers() {
			loading.value = true;
			try {
				const data = (await api.get("/api/v1/users?page=0&size=999")).data.data;
				allUsers.value = data.content || data || [];
			} catch (err) {
				toast.error(err.response?.data?.message || "Failed to load users.");
			} finally {
				loading.value = false;
			}
		}
		async function fetchRoles() {
			try {
				roles.value = (await api.get("/api/v1/roles")).data.data;
			} catch {
				roles.value = [];
			}
		}
		onMounted(() => {
			fetchUsers();
			fetchRoles();
			document.addEventListener("click", handleOutsideClick);
		});
		onBeforeUnmount(() => {
			document.removeEventListener("click", handleOutsideClick);
		});
		const filterRef = ref(null);
		function handleOutsideClick(e) {
			if (filterRef.value && !filterRef.value.contains(e.target)) filterOpen.value = false;
		}
		function openCreate() {
			form.value = {
				id: null,
				username: "",
				fullname: "",
				email: "",
				password: "",
				phone: "",
				roleIds: []
			};
			avatarPreview.value = null;
			avatarFile.value = null;
			formError.value = null;
			clearFieldErrors();
			showPassword.value = false;
			drawerMode.value = "create";
			showDrawer.value = true;
		}
		function openEdit(user) {
			form.value = {
				id: user.id,
				username: user.username,
				fullname: user.fullname || "",
				email: user.email || "",
				phone: user.phone || "",
				password: "",
				roleIds: (user.roles || []).map((r) => r.id)
			};
			avatarPreview.value = user.avatar || null;
			avatarFile.value = null;
			formError.value = null;
			clearFieldErrors();
			showPassword.value = false;
			drawerMode.value = "edit";
			showDrawer.value = true;
		}
		async function saveUser() {
			formError.value = null;
			if (!validateForm()) return;
			saving.value = true;
			try {
				const payload = {
					username: form.value.username,
					fullname: form.value.fullname || null,
					roleIds: form.value.roleIds
				};
				if (drawerMode.value === "create") {
					payload.password = form.value.password;
					await api.post("/api/v1/users", payload);
					toast.success("User created!");
				} else {
					if (form.value.password) payload.password = form.value.password;
					await api.put(`/api/v1/users/${form.value.id}`, payload);
					toast.success("User updated!");
				}
				showDrawer.value = false;
				fetchUsers();
			} catch (err) {
				formError.value = err.response?.data?.data?.message || err.response?.data?.message || "Failed to save user.";
			} finally {
				saving.value = false;
			}
		}
		function toggleRole(roleId) {
			const idx = form.value.roleIds.indexOf(roleId);
			if (idx === -1) form.value.roleIds.push(roleId);
			else form.value.roleIds.splice(idx, 1);
		}
		function openDelete(user) {
			deletingUser.value = user;
			deleteAdminUsername.value = "";
			deleteAdminPassword.value = "";
			deleteError.value = "";
			showDeletePassword.value = false;
			showDeleteDialog.value = true;
		}
		function cancelDelete() {
			showDeleteDialog.value = false;
			deletingUser.value = null;
		}
		async function confirmDelete() {
			if (!deleteAdminUsername.value || !deleteAdminPassword.value) {
				deleteError.value = "Please enter admin username and password.";
				return;
			}
			toast.success(`User "${deletingUser.value.username}" will be deleted (mock).`);
			showDeleteDialog.value = false;
			deletingUser.value = null;
		}
		function getRoleName(user) {
			return (user.roles || [])[0]?.name || "-";
		}
		function getRoleBadgeClass(roleName) {
			if (!roleName || roleName === "-") return "bg-zinc-100 dark:bg-zinc-800 text-zinc-500 dark:text-zinc-400";
			if (roleName.toLowerCase() === "admin") return "bg-zinc-900 dark:bg-white text-white dark:text-zinc-900";
			return "bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-300";
		}
		function formatDate(d) {
			if (!d) return "-";
			return new Date(d).toLocaleDateString("en-GB", {
				day: "2-digit",
				month: "short",
				year: "numeric"
			});
		}
		function getUserInitials(user) {
			return (user.fullname || user.username).split(" ").map((n) => n[0]).join("").toUpperCase().slice(0, 2);
		}
		const avatarColors = [
			"bg-zinc-700",
			"bg-zinc-600",
			"bg-zinc-500",
			"bg-zinc-800",
			"bg-zinc-900"
		];
		function getAvatarColor(user) {
			return avatarColors[(user.id || "").split("").reduce((a, c) => a + c.charCodeAt(0), 0) % avatarColors.length];
		}
		return (_ctx, _push, _parent, _attrs) => {
			_push(ssrRenderComponent(_sfc_main$2, _attrs, {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) {
						_push(`<div class="pb-6" data-v-d7861a18${_scopeId}><div class="mb-6" data-v-d7861a18${_scopeId}><h1 class="text-xl font-bold tracking-tight text-zinc-900 dark:text-zinc-100" data-v-d7861a18${_scopeId}>User Management</h1><p class="text-xs text-zinc-500 dark:text-zinc-400 mt-0.5" data-v-d7861a18${_scopeId}>Manage system users, roles, and permissions.</p></div><div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-5" data-v-d7861a18${_scopeId}>`);
						_push(ssrRenderComponent(_sfc_main$11, {
							modelValue: searchQuery.value,
							"onUpdate:modelValue": ($event) => searchQuery.value = $event,
							placeholder: "Search by name, username, phone, or role…",
							class: "w-full max-w-sm"
						}, null, _parent, _scopeId));
						_push(`<div class="flex items-center gap-2 w-full sm:w-auto" data-v-d7861a18${_scopeId}><div class="relative flex-1 sm:flex-none" data-v-d7861a18${_scopeId}><button class="w-full flex items-center justify-center gap-2 h-9 px-3 rounded-md border border-border bg-background hover:bg-accent text-foreground text-sm font-medium transition-colors" data-v-d7861a18${_scopeId}><svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" data-v-d7861a18${_scopeId}><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3" data-v-d7861a18${_scopeId}></polygon></svg><span data-v-d7861a18${_scopeId}>Filter</span>`);
						if (activeFilterCount.value > 0) _push(`<span class="inline-flex items-center justify-center h-4.5 min-w-[18px] px-1 rounded-full bg-primary text-primary-foreground text-[10px] font-bold leading-none" data-v-d7861a18${_scopeId}>${ssrInterpolate(activeFilterCount.value)}</span>`);
						else _push(`<!---->`);
						_push(ssrRenderComponent(unref(ChevronDown), {
							class: ["h-3 w-3 text-muted-foreground", filterOpen.value ? "rotate-180" : ""],
							style: { "transition": "transform 0.2s" }
						}, null, _parent, _scopeId));
						_push(`</button>`);
						if (filterOpen.value) {
							_push(`<div class="absolute left-0 sm:left-auto sm:right-0 top-full mt-1 z-30 w-[calc(100vw-2.5rem)] sm:w-64 max-w-[280px] sm:max-w-none bg-card border border-border rounded-lg shadow-xl overflow-hidden" data-v-d7861a18${_scopeId}><div class="flex items-center justify-between px-3 py-2.5 border-b border-border" data-v-d7861a18${_scopeId}><span class="text-xs font-semibold text-foreground uppercase tracking-wide" data-v-d7861a18${_scopeId}>Filter</span>`);
							if (activeFilterCount.value > 0) _push(`<button class="text-xs text-red-500 hover:text-red-600 font-medium transition-colors" data-v-d7861a18${_scopeId}>Clear all</button>`);
							else _push(`<!---->`);
							_push(`</div><div class="px-3 pt-3 pb-2" data-v-d7861a18${_scopeId}><p class="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider mb-2" data-v-d7861a18${_scopeId}>Role</p><div class="space-y-1" data-v-d7861a18${_scopeId}><!--[-->`);
							ssrRenderList(availableRoles.value, (role) => {
								_push(`<button class="w-full flex items-center justify-between px-2 py-1.5 rounded-md hover:bg-muted/50 cursor-pointer transition-colors outline-none" data-v-d7861a18${_scopeId}><span class="text-sm font-medium text-foreground select-none" data-v-d7861a18${_scopeId}>${ssrInterpolate(role.name)}</span>`);
								if (activeFilters.value.roles.includes(role.slug)) _push(ssrRenderComponent(unref(Check), { class: "h-4 w-4 text-foreground" }, null, _parent, _scopeId));
								else _push(`<!---->`);
								_push(`</button>`);
							});
							_push(`<!--]-->`);
							if (availableRoles.value.length === 0) _push(`<p class="text-xs text-muted-foreground px-2 py-1" data-v-d7861a18${_scopeId}>No roles available.</p>`);
							else _push(`<!---->`);
							_push(`</div></div><div class="mx-3 border-t border-border" data-v-d7861a18${_scopeId}></div><div class="px-3 pt-2 pb-3" data-v-d7861a18${_scopeId}><p class="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider mb-2" data-v-d7861a18${_scopeId}>Telepon</p><div class="space-y-1" data-v-d7861a18${_scopeId}><button class="w-full flex items-center justify-between px-2 py-1.5 rounded-md hover:bg-muted/50 cursor-pointer transition-colors outline-none" data-v-d7861a18${_scopeId}><span class="text-sm font-medium text-foreground select-none" data-v-d7861a18${_scopeId}>Punya Nomor Telepon</span>`);
							if (activeFilters.value.hasPhone === true) _push(ssrRenderComponent(unref(Check), { class: "h-4 w-4 text-foreground" }, null, _parent, _scopeId));
							else _push(`<!---->`);
							_push(`</button><button class="w-full flex items-center justify-between px-2 py-1.5 rounded-md hover:bg-muted/50 cursor-pointer transition-colors outline-none" data-v-d7861a18${_scopeId}><span class="text-sm font-medium text-foreground select-none" data-v-d7861a18${_scopeId}>Tanpa Nomor Telepon</span>`);
							if (activeFilters.value.hasPhone === false) _push(ssrRenderComponent(unref(Check), { class: "h-4 w-4 text-foreground" }, null, _parent, _scopeId));
							else _push(`<!---->`);
							_push(`</button></div></div></div>`);
						} else _push(`<!---->`);
						_push(`</div>`);
						if (unref(can)("user.store")) _push(ssrRenderComponent(unref(_sfc_main$3), {
							onClick: openCreate,
							size: "sm",
							class: "flex-1 sm:flex-none flex items-center justify-center gap-2 bg-primary hover:bg-primary/90 text-primary-foreground"
						}, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) {
									_push(ssrRenderComponent(unref(Plus), { class: "h-4 w-4" }, null, _parent, _scopeId));
									_push(`<span data-v-d7861a18${_scopeId}>Add User</span>`);
								} else return [createVNode(unref(Plus), { class: "h-4 w-4" }), createVNode("span", null, "Add User")];
							}),
							_: 1
						}, _parent, _scopeId));
						else _push(`<!---->`);
						_push(`</div></div>`);
						_push(ssrRenderComponent(_sfc_main$9, { class: "border-border shadow-sm overflow-hidden" }, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) _push(ssrRenderComponent(_sfc_main$10, { class: "p-0" }, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(ssrRenderComponent(_sfc_main$1, {
											data: pagedUsers.value,
											columns: columns.value,
											page: page.value,
											"page-size": pageSize.value,
											total: total.value,
											loading: loading.value,
											sortable: true,
											"empty-message": "No users found.",
											"onUpdate:page": ($event) => page.value = $event,
											"onUpdate:pageSize": ($event) => pageSize.value = $event
										}, {
											"cell-username": withCtx(({ item }, _push, _parent, _scopeId) => {
												if (_push) {
													_push(`<div class="flex items-center gap-3" data-v-d7861a18${_scopeId}><div class="relative shrink-0" data-v-d7861a18${_scopeId}>`);
													if (item.avatar) _push(`<img${ssrRenderAttr("src", item.avatar)}${ssrRenderAttr("alt", item.fullname || item.username)} class="w-9 h-9 rounded-full border border-border bg-muted object-cover" data-v-d7861a18${_scopeId}>`);
													else _push(`<!---->`);
													_push(`<div class="${ssrRenderClass(["w-9 h-9 rounded-full flex items-center justify-center text-white text-xs font-bold shrink-0", getAvatarColor(item)])}" style="${ssrRenderStyle(item.avatar ? "display:none" : "")}" data-v-d7861a18${_scopeId}>${ssrInterpolate(getUserInitials(item))}</div></div><div class="flex flex-col min-w-0" data-v-d7861a18${_scopeId}><span class="font-semibold text-foreground text-sm truncate" data-v-d7861a18${_scopeId}>${ssrInterpolate(item.fullname || item.username)}</span><span class="text-xs text-muted-foreground truncate" data-v-d7861a18${_scopeId}>${ssrInterpolate(item.username)}</span></div></div>`);
												} else return [createVNode("div", { class: "flex items-center gap-3" }, [createVNode("div", { class: "relative shrink-0" }, [item.avatar ? (openBlock(), createBlock("img", {
													key: 0,
													src: item.avatar,
													alt: item.fullname || item.username,
													class: "w-9 h-9 rounded-full border border-border bg-muted object-cover",
													onError: (e) => {
														e.target.style.display = "none";
														e.target.nextSibling.style.display = "flex";
													}
												}, null, 40, [
													"src",
													"alt",
													"onError"
												])) : createCommentVNode("", true), createVNode("div", {
													class: ["w-9 h-9 rounded-full flex items-center justify-center text-white text-xs font-bold shrink-0", getAvatarColor(item)],
													style: item.avatar ? "display:none" : ""
												}, toDisplayString(getUserInitials(item)), 7)]), createVNode("div", { class: "flex flex-col min-w-0" }, [createVNode("span", { class: "font-semibold text-foreground text-sm truncate" }, toDisplayString(item.fullname || item.username), 1), createVNode("span", { class: "text-xs text-muted-foreground truncate" }, toDisplayString(item.username), 1)])])];
											}),
											"cell-roles": withCtx(({ item }, _push, _parent, _scopeId) => {
												if (_push) {
													_push(`<span class="${ssrRenderClass(["inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[10px] font-bold uppercase tracking-wide", getRoleBadgeClass(getRoleName(item))])}" data-v-d7861a18${_scopeId}>`);
													_push(ssrRenderComponent(unref(Shield), { class: "h-3 w-3 opacity-70" }, null, _parent, _scopeId));
													_push(` ${ssrInterpolate(getRoleName(item))}</span>`);
												} else return [createVNode("span", { class: ["inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[10px] font-bold uppercase tracking-wide", getRoleBadgeClass(getRoleName(item))] }, [createVNode(unref(Shield), { class: "h-3 w-3 opacity-70" }), createTextVNode(" " + toDisplayString(getRoleName(item)), 1)], 2)];
											}),
											"cell-phone": withCtx(({ item }, _push, _parent, _scopeId) => {
												if (_push) _push(`<span class="text-sm text-muted-foreground" data-v-d7861a18${_scopeId}>${ssrInterpolate(item.phone || "-")}</span>`);
												else return [createVNode("span", { class: "text-sm text-muted-foreground" }, toDisplayString(item.phone || "-"), 1)];
											}),
											"cell-createdAt": withCtx(({ item }, _push, _parent, _scopeId) => {
												if (_push) _push(`<span class="text-sm text-muted-foreground font-medium" data-v-d7861a18${_scopeId}>${ssrInterpolate(formatDate(item.createdAt))}</span>`);
												else return [createVNode("span", { class: "text-sm text-muted-foreground font-medium" }, toDisplayString(formatDate(item.createdAt)), 1)];
											}),
											actions: withCtx(({ item }, _push, _parent, _scopeId) => {
												if (_push) _push(ssrRenderComponent(unref(_sfc_main$4), null, {
													default: withCtx((_, _push, _parent, _scopeId) => {
														if (_push) {
															_push(ssrRenderComponent(unref(_sfc_main$5), { "as-child": "" }, {
																default: withCtx((_, _push, _parent, _scopeId) => {
																	if (_push) {
																		_push(`<button class="p-1.5 hover:bg-accent rounded-md transition-colors outline-none" data-v-d7861a18${_scopeId}>`);
																		_push(ssrRenderComponent(unref(MoreVertical), { class: "h-4 w-4 text-muted-foreground" }, null, _parent, _scopeId));
																		_push(`</button>`);
																	} else return [createVNode("button", { class: "p-1.5 hover:bg-accent rounded-md transition-colors outline-none" }, [createVNode(unref(MoreVertical), { class: "h-4 w-4 text-muted-foreground" })])];
																}),
																_: 2
															}, _parent, _scopeId));
															_push(ssrRenderComponent(unref(_sfc_main$6), {
																align: "end",
																class: "w-40"
															}, {
																default: withCtx((_, _push, _parent, _scopeId) => {
																	if (_push) {
																		_push(ssrRenderComponent(unref(_sfc_main$7), {
																			onClick: ($event) => openEdit(item),
																			class: "gap-2 cursor-pointer text-foreground"
																		}, {
																			default: withCtx((_, _push, _parent, _scopeId) => {
																				if (_push) {
																					_push(ssrRenderComponent(unref(Pencil), { class: "h-3.5 w-3.5" }, null, _parent, _scopeId));
																					_push(` Edit `);
																				} else return [createVNode(unref(Pencil), { class: "h-3.5 w-3.5" }), createTextVNode(" Edit ")];
																			}),
																			_: 2
																		}, _parent, _scopeId));
																		_push(ssrRenderComponent(unref(_sfc_main$8), null, null, _parent, _scopeId));
																		_push(ssrRenderComponent(unref(_sfc_main$7), {
																			onClick: ($event) => openDelete(item),
																			class: "gap-2 cursor-pointer text-red-600 dark:text-red-400 focus:text-red-600 dark:focus:text-red-400"
																		}, {
																			default: withCtx((_, _push, _parent, _scopeId) => {
																				if (_push) {
																					_push(ssrRenderComponent(unref(Trash2), { class: "h-3.5 w-3.5" }, null, _parent, _scopeId));
																					_push(` Delete `);
																				} else return [createVNode(unref(Trash2), { class: "h-3.5 w-3.5" }), createTextVNode(" Delete ")];
																			}),
																			_: 2
																		}, _parent, _scopeId));
																	} else return [
																		createVNode(unref(_sfc_main$7), {
																			onClick: ($event) => openEdit(item),
																			class: "gap-2 cursor-pointer text-foreground"
																		}, {
																			default: withCtx(() => [createVNode(unref(Pencil), { class: "h-3.5 w-3.5" }), createTextVNode(" Edit ")]),
																			_: 1
																		}, 8, ["onClick"]),
																		createVNode(unref(_sfc_main$8)),
																		createVNode(unref(_sfc_main$7), {
																			onClick: ($event) => openDelete(item),
																			class: "gap-2 cursor-pointer text-red-600 dark:text-red-400 focus:text-red-600 dark:focus:text-red-400"
																		}, {
																			default: withCtx(() => [createVNode(unref(Trash2), { class: "h-3.5 w-3.5" }), createTextVNode(" Delete ")]),
																			_: 1
																		}, 8, ["onClick"])
																	];
																}),
																_: 2
															}, _parent, _scopeId));
														} else return [createVNode(unref(_sfc_main$5), { "as-child": "" }, {
															default: withCtx(() => [createVNode("button", { class: "p-1.5 hover:bg-accent rounded-md transition-colors outline-none" }, [createVNode(unref(MoreVertical), { class: "h-4 w-4 text-muted-foreground" })])]),
															_: 1
														}), createVNode(unref(_sfc_main$6), {
															align: "end",
															class: "w-40"
														}, {
															default: withCtx(() => [
																createVNode(unref(_sfc_main$7), {
																	onClick: ($event) => openEdit(item),
																	class: "gap-2 cursor-pointer text-foreground"
																}, {
																	default: withCtx(() => [createVNode(unref(Pencil), { class: "h-3.5 w-3.5" }), createTextVNode(" Edit ")]),
																	_: 1
																}, 8, ["onClick"]),
																createVNode(unref(_sfc_main$8)),
																createVNode(unref(_sfc_main$7), {
																	onClick: ($event) => openDelete(item),
																	class: "gap-2 cursor-pointer text-red-600 dark:text-red-400 focus:text-red-600 dark:focus:text-red-400"
																}, {
																	default: withCtx(() => [createVNode(unref(Trash2), { class: "h-3.5 w-3.5" }), createTextVNode(" Delete ")]),
																	_: 1
																}, 8, ["onClick"])
															]),
															_: 2
														}, 1024)];
													}),
													_: 2
												}, _parent, _scopeId));
												else return [createVNode(unref(_sfc_main$4), null, {
													default: withCtx(() => [createVNode(unref(_sfc_main$5), { "as-child": "" }, {
														default: withCtx(() => [createVNode("button", { class: "p-1.5 hover:bg-accent rounded-md transition-colors outline-none" }, [createVNode(unref(MoreVertical), { class: "h-4 w-4 text-muted-foreground" })])]),
														_: 1
													}), createVNode(unref(_sfc_main$6), {
														align: "end",
														class: "w-40"
													}, {
														default: withCtx(() => [
															createVNode(unref(_sfc_main$7), {
																onClick: ($event) => openEdit(item),
																class: "gap-2 cursor-pointer text-foreground"
															}, {
																default: withCtx(() => [createVNode(unref(Pencil), { class: "h-3.5 w-3.5" }), createTextVNode(" Edit ")]),
																_: 1
															}, 8, ["onClick"]),
															createVNode(unref(_sfc_main$8)),
															createVNode(unref(_sfc_main$7), {
																onClick: ($event) => openDelete(item),
																class: "gap-2 cursor-pointer text-red-600 dark:text-red-400 focus:text-red-600 dark:focus:text-red-400"
															}, {
																default: withCtx(() => [createVNode(unref(Trash2), { class: "h-3.5 w-3.5" }), createTextVNode(" Delete ")]),
																_: 1
															}, 8, ["onClick"])
														]),
														_: 2
													}, 1024)]),
													_: 2
												}, 1024)];
											}),
											_: 1
										}, _parent, _scopeId));
										else return [createVNode(_sfc_main$1, {
											data: pagedUsers.value,
											columns: columns.value,
											page: page.value,
											"page-size": pageSize.value,
											total: total.value,
											loading: loading.value,
											sortable: true,
											"empty-message": "No users found.",
											"onUpdate:page": ($event) => page.value = $event,
											"onUpdate:pageSize": ($event) => pageSize.value = $event
										}, {
											"cell-username": withCtx(({ item }) => [createVNode("div", { class: "flex items-center gap-3" }, [createVNode("div", { class: "relative shrink-0" }, [item.avatar ? (openBlock(), createBlock("img", {
												key: 0,
												src: item.avatar,
												alt: item.fullname || item.username,
												class: "w-9 h-9 rounded-full border border-border bg-muted object-cover",
												onError: (e) => {
													e.target.style.display = "none";
													e.target.nextSibling.style.display = "flex";
												}
											}, null, 40, [
												"src",
												"alt",
												"onError"
											])) : createCommentVNode("", true), createVNode("div", {
												class: ["w-9 h-9 rounded-full flex items-center justify-center text-white text-xs font-bold shrink-0", getAvatarColor(item)],
												style: item.avatar ? "display:none" : ""
											}, toDisplayString(getUserInitials(item)), 7)]), createVNode("div", { class: "flex flex-col min-w-0" }, [createVNode("span", { class: "font-semibold text-foreground text-sm truncate" }, toDisplayString(item.fullname || item.username), 1), createVNode("span", { class: "text-xs text-muted-foreground truncate" }, toDisplayString(item.username), 1)])])]),
											"cell-roles": withCtx(({ item }) => [createVNode("span", { class: ["inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[10px] font-bold uppercase tracking-wide", getRoleBadgeClass(getRoleName(item))] }, [createVNode(unref(Shield), { class: "h-3 w-3 opacity-70" }), createTextVNode(" " + toDisplayString(getRoleName(item)), 1)], 2)]),
											"cell-phone": withCtx(({ item }) => [createVNode("span", { class: "text-sm text-muted-foreground" }, toDisplayString(item.phone || "-"), 1)]),
											"cell-createdAt": withCtx(({ item }) => [createVNode("span", { class: "text-sm text-muted-foreground font-medium" }, toDisplayString(formatDate(item.createdAt)), 1)]),
											actions: withCtx(({ item }) => [createVNode(unref(_sfc_main$4), null, {
												default: withCtx(() => [createVNode(unref(_sfc_main$5), { "as-child": "" }, {
													default: withCtx(() => [createVNode("button", { class: "p-1.5 hover:bg-accent rounded-md transition-colors outline-none" }, [createVNode(unref(MoreVertical), { class: "h-4 w-4 text-muted-foreground" })])]),
													_: 1
												}), createVNode(unref(_sfc_main$6), {
													align: "end",
													class: "w-40"
												}, {
													default: withCtx(() => [
														createVNode(unref(_sfc_main$7), {
															onClick: ($event) => openEdit(item),
															class: "gap-2 cursor-pointer text-foreground"
														}, {
															default: withCtx(() => [createVNode(unref(Pencil), { class: "h-3.5 w-3.5" }), createTextVNode(" Edit ")]),
															_: 1
														}, 8, ["onClick"]),
														createVNode(unref(_sfc_main$8)),
														createVNode(unref(_sfc_main$7), {
															onClick: ($event) => openDelete(item),
															class: "gap-2 cursor-pointer text-red-600 dark:text-red-400 focus:text-red-600 dark:focus:text-red-400"
														}, {
															default: withCtx(() => [createVNode(unref(Trash2), { class: "h-3.5 w-3.5" }), createTextVNode(" Delete ")]),
															_: 1
														}, 8, ["onClick"])
													]),
													_: 2
												}, 1024)]),
												_: 2
											}, 1024)]),
											_: 1
										}, 8, [
											"data",
											"columns",
											"page",
											"page-size",
											"total",
											"loading",
											"onUpdate:page",
											"onUpdate:pageSize"
										])];
									}),
									_: 1
								}, _parent, _scopeId));
								else return [createVNode(_sfc_main$10, { class: "p-0" }, {
									default: withCtx(() => [createVNode(_sfc_main$1, {
										data: pagedUsers.value,
										columns: columns.value,
										page: page.value,
										"page-size": pageSize.value,
										total: total.value,
										loading: loading.value,
										sortable: true,
										"empty-message": "No users found.",
										"onUpdate:page": ($event) => page.value = $event,
										"onUpdate:pageSize": ($event) => pageSize.value = $event
									}, {
										"cell-username": withCtx(({ item }) => [createVNode("div", { class: "flex items-center gap-3" }, [createVNode("div", { class: "relative shrink-0" }, [item.avatar ? (openBlock(), createBlock("img", {
											key: 0,
											src: item.avatar,
											alt: item.fullname || item.username,
											class: "w-9 h-9 rounded-full border border-border bg-muted object-cover",
											onError: (e) => {
												e.target.style.display = "none";
												e.target.nextSibling.style.display = "flex";
											}
										}, null, 40, [
											"src",
											"alt",
											"onError"
										])) : createCommentVNode("", true), createVNode("div", {
											class: ["w-9 h-9 rounded-full flex items-center justify-center text-white text-xs font-bold shrink-0", getAvatarColor(item)],
											style: item.avatar ? "display:none" : ""
										}, toDisplayString(getUserInitials(item)), 7)]), createVNode("div", { class: "flex flex-col min-w-0" }, [createVNode("span", { class: "font-semibold text-foreground text-sm truncate" }, toDisplayString(item.fullname || item.username), 1), createVNode("span", { class: "text-xs text-muted-foreground truncate" }, toDisplayString(item.username), 1)])])]),
										"cell-roles": withCtx(({ item }) => [createVNode("span", { class: ["inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[10px] font-bold uppercase tracking-wide", getRoleBadgeClass(getRoleName(item))] }, [createVNode(unref(Shield), { class: "h-3 w-3 opacity-70" }), createTextVNode(" " + toDisplayString(getRoleName(item)), 1)], 2)]),
										"cell-phone": withCtx(({ item }) => [createVNode("span", { class: "text-sm text-muted-foreground" }, toDisplayString(item.phone || "-"), 1)]),
										"cell-createdAt": withCtx(({ item }) => [createVNode("span", { class: "text-sm text-muted-foreground font-medium" }, toDisplayString(formatDate(item.createdAt)), 1)]),
										actions: withCtx(({ item }) => [createVNode(unref(_sfc_main$4), null, {
											default: withCtx(() => [createVNode(unref(_sfc_main$5), { "as-child": "" }, {
												default: withCtx(() => [createVNode("button", { class: "p-1.5 hover:bg-accent rounded-md transition-colors outline-none" }, [createVNode(unref(MoreVertical), { class: "h-4 w-4 text-muted-foreground" })])]),
												_: 1
											}), createVNode(unref(_sfc_main$6), {
												align: "end",
												class: "w-40"
											}, {
												default: withCtx(() => [
													createVNode(unref(_sfc_main$7), {
														onClick: ($event) => openEdit(item),
														class: "gap-2 cursor-pointer text-foreground"
													}, {
														default: withCtx(() => [createVNode(unref(Pencil), { class: "h-3.5 w-3.5" }), createTextVNode(" Edit ")]),
														_: 1
													}, 8, ["onClick"]),
													createVNode(unref(_sfc_main$8)),
													createVNode(unref(_sfc_main$7), {
														onClick: ($event) => openDelete(item),
														class: "gap-2 cursor-pointer text-red-600 dark:text-red-400 focus:text-red-600 dark:focus:text-red-400"
													}, {
														default: withCtx(() => [createVNode(unref(Trash2), { class: "h-3.5 w-3.5" }), createTextVNode(" Delete ")]),
														_: 1
													}, 8, ["onClick"])
												]),
												_: 2
											}, 1024)]),
											_: 2
										}, 1024)]),
										_: 1
									}, 8, [
										"data",
										"columns",
										"page",
										"page-size",
										"total",
										"loading",
										"onUpdate:page",
										"onUpdate:pageSize"
									])]),
									_: 1
								})];
							}),
							_: 1
						}, _parent, _scopeId));
						_push(`</div>`);
						ssrRenderTeleport(_push, (_push) => {
							if (showDrawer.value) _push(`<div class="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm" data-v-d7861a18${_scopeId}></div>`);
							else _push(`<!---->`);
							if (showDrawer.value) {
								_push(`<div class="fixed z-50 flex flex-col bg-card border-border shadow-2xl inset-0 sm:inset-y-0 sm:left-auto sm:right-0 sm:w-full sm:max-w-md sm:border-l" data-v-d7861a18${_scopeId}><div class="flex items-center justify-between px-6 py-4 border-b border-border bg-muted/30 shrink-0" data-v-d7861a18${_scopeId}><div data-v-d7861a18${_scopeId}><h3 class="font-semibold text-lg text-foreground" data-v-d7861a18${_scopeId}>${ssrInterpolate(drawerMode.value === "create" ? "Add User" : "Edit User")}</h3><p class="text-xs text-muted-foreground mt-0.5" data-v-d7861a18${_scopeId}>${ssrInterpolate(drawerMode.value === "create" ? "Create a new user account." : "Update user information.")}</p></div><button class="p-2 rounded-md hover:bg-accent transition-colors text-muted-foreground hover:text-foreground" data-v-d7861a18${_scopeId}>`);
								_push(ssrRenderComponent(unref(X), { class: "h-4 w-4" }, null, _parent, _scopeId));
								_push(`</button></div><div class="flex-1 overflow-y-auto px-6 py-5 space-y-5" data-v-d7861a18${_scopeId}>`);
								if (formError.value) _push(`<div class="rounded-md border border-red-200 dark:border-red-900 bg-red-50 dark:bg-red-950/30 p-3" data-v-d7861a18${_scopeId}><p class="text-sm text-red-600 dark:text-red-400" data-v-d7861a18${_scopeId}>${ssrInterpolate(formError.value)}</p></div>`);
								else _push(`<!---->`);
								_push(`<div class="flex items-center gap-4 p-4 rounded-lg bg-muted/50 border border-border" data-v-d7861a18${_scopeId}><div class="relative shrink-0 group" data-v-d7861a18${_scopeId}>`);
								if (avatarPreview.value) _push(`<img${ssrRenderAttr("src", avatarPreview.value)} alt="Avatar preview" class="w-16 h-16 rounded-full object-cover border-2 border-border" data-v-d7861a18${_scopeId}>`);
								else _push(`<div class="${ssrRenderClass(["w-16 h-16 rounded-full flex items-center justify-center text-white text-xl font-bold border-2 border-border", form.value.id ? getAvatarColor({ id: form.value.id }) : "bg-zinc-700"])}" data-v-d7861a18${_scopeId}>${ssrInterpolate(form.value.fullname || form.value.username ? getUserInitials({
									fullname: form.value.fullname,
									username: form.value.username || "?"
								}) : "?")}</div>`);
								_push(`<button type="button" class="absolute inset-0 rounded-full bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center" data-v-d7861a18${_scopeId}><svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" data-v-d7861a18${_scopeId}><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" data-v-d7861a18${_scopeId}></path><polyline points="17 8 12 3 7 8" data-v-d7861a18${_scopeId}></polyline><line x1="12" y1="3" x2="12" y2="15" data-v-d7861a18${_scopeId}></line></svg></button></div><div class="flex-1 min-w-0" data-v-d7861a18${_scopeId}><p class="font-semibold text-foreground truncate" data-v-d7861a18${_scopeId}>${ssrInterpolate(form.value.fullname || form.value.username || "New User")}</p><p class="text-xs text-muted-foreground mb-2" data-v-d7861a18${_scopeId}>@${ssrInterpolate(form.value.username || "username")}</p><div class="flex items-center gap-2" data-v-d7861a18${_scopeId}><button type="button" class="text-xs font-medium px-2.5 py-1 rounded-md border border-border bg-background hover:bg-accent text-foreground transition-colors" data-v-d7861a18${_scopeId}>Upload Photo</button>`);
								if (avatarPreview.value) _push(`<button type="button" class="text-xs font-medium px-2.5 py-1 rounded-md text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-950/30 transition-colors" data-v-d7861a18${_scopeId}>Remove</button>`);
								else _push(`<!---->`);
								_push(`</div></div><input type="file" accept="image/*" class="hidden" data-v-d7861a18${_scopeId}></div><div class="space-y-1.5" data-v-d7861a18${_scopeId}><label class="text-sm font-medium text-foreground" for="dw-username" data-v-d7861a18${_scopeId}>Username <span class="text-red-500" data-v-d7861a18${_scopeId}>*</span></label><input id="dw-username"${ssrRenderAttr("value", form.value.username)} type="text" placeholder="e.g. john_doe"${ssrIncludeBooleanAttr(saving.value) ? " disabled" : ""} class="${ssrRenderClass(["flex h-10 w-full rounded-md border px-3 py-2 text-sm text-foreground bg-background placeholder:text-muted-foreground outline-none transition disabled:opacity-50", fieldErrors.value.username ? "border-red-400 focus:ring-2 focus:ring-red-300" : "border-input focus:ring-2 focus:ring-ring/30 focus:border-ring"])}" data-v-d7861a18${_scopeId}>`);
								if (fieldErrors.value.username) _push(`<p class="text-xs text-red-500 flex items-center gap-1 mt-1" data-v-d7861a18${_scopeId}><svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" data-v-d7861a18${_scopeId}><circle cx="12" cy="12" r="10" data-v-d7861a18${_scopeId}></circle><line x1="12" y1="8" x2="12" y2="12" data-v-d7861a18${_scopeId}></line><line x1="12" y1="16" x2="12.01" y2="16" data-v-d7861a18${_scopeId}></line></svg> ${ssrInterpolate(fieldErrors.value.username)}</p>`);
								else _push(`<!---->`);
								_push(`</div><div class="space-y-1.5" data-v-d7861a18${_scopeId}><label class="text-sm font-medium text-foreground" for="dw-fullname" data-v-d7861a18${_scopeId}>Full Name</label><input id="dw-fullname"${ssrRenderAttr("value", form.value.fullname)} type="text" placeholder="e.g. John Doe"${ssrIncludeBooleanAttr(saving.value) ? " disabled" : ""} class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground focus:ring-2 focus:ring-ring/30 focus:border-ring outline-none transition disabled:opacity-50" data-v-d7861a18${_scopeId}></div><div class="space-y-1.5" data-v-d7861a18${_scopeId}><label class="text-sm font-medium text-foreground" for="dw-email" data-v-d7861a18${_scopeId}>Email</label><input id="dw-email"${ssrRenderAttr("value", form.value.email)} type="text" placeholder="e.g. john@example.com"${ssrIncludeBooleanAttr(saving.value) ? " disabled" : ""} class="${ssrRenderClass(["flex h-10 w-full rounded-md border px-3 py-2 text-sm text-foreground bg-background placeholder:text-muted-foreground outline-none transition disabled:opacity-50", fieldErrors.value.email ? "border-red-400 focus:ring-2 focus:ring-red-300" : "border-input focus:ring-2 focus:ring-ring/30 focus:border-ring"])}" data-v-d7861a18${_scopeId}>`);
								if (fieldErrors.value.email) _push(`<p class="text-xs text-red-500 flex items-center gap-1 mt-1" data-v-d7861a18${_scopeId}><svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" data-v-d7861a18${_scopeId}><circle cx="12" cy="12" r="10" data-v-d7861a18${_scopeId}></circle><line x1="12" y1="8" x2="12" y2="12" data-v-d7861a18${_scopeId}></line><line x1="12" y1="16" x2="12.01" y2="16" data-v-d7861a18${_scopeId}></line></svg> ${ssrInterpolate(fieldErrors.value.email)}</p>`);
								else _push(`<!---->`);
								_push(`</div><div class="space-y-1.5" data-v-d7861a18${_scopeId}><label class="text-sm font-medium text-foreground" for="dw-phone" data-v-d7861a18${_scopeId}>Phone</label><input id="dw-phone"${ssrRenderAttr("value", form.value.phone)} type="tel" placeholder="e.g. 6281234567890"${ssrIncludeBooleanAttr(saving.value) ? " disabled" : ""} class="${ssrRenderClass(["flex h-10 w-full rounded-md border px-3 py-2 text-sm text-foreground bg-background placeholder:text-muted-foreground outline-none transition disabled:opacity-50", fieldErrors.value.phone ? "border-red-400 focus:ring-2 focus:ring-red-300" : "border-input focus:ring-2 focus:ring-ring/30 focus:border-ring"])}" data-v-d7861a18${_scopeId}>`);
								if (fieldErrors.value.phone) _push(`<p class="text-xs text-red-500 flex items-center gap-1 mt-1" data-v-d7861a18${_scopeId}><svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" data-v-d7861a18${_scopeId}><circle cx="12" cy="12" r="10" data-v-d7861a18${_scopeId}></circle><line x1="12" y1="8" x2="12" y2="12" data-v-d7861a18${_scopeId}></line><line x1="12" y1="16" x2="12.01" y2="16" data-v-d7861a18${_scopeId}></line></svg> ${ssrInterpolate(fieldErrors.value.phone)}</p>`);
								else _push(`<!---->`);
								_push(`</div><div class="space-y-1.5" data-v-d7861a18${_scopeId}><label class="text-sm font-medium text-foreground" for="dw-password" data-v-d7861a18${_scopeId}> Password `);
								if (drawerMode.value === "create") _push(`<span class="text-red-500" data-v-d7861a18${_scopeId}>*</span>`);
								else _push(`<span class="text-muted-foreground text-xs font-normal" data-v-d7861a18${_scopeId}> (leave blank to keep current)</span>`);
								_push(`</label><div class="relative" data-v-d7861a18${_scopeId}><input id="dw-password"${ssrRenderDynamicModel(showPassword.value ? "text" : "password", form.value.password, null)}${ssrRenderAttr("type", showPassword.value ? "text" : "password")}${ssrRenderAttr("placeholder", drawerMode.value === "create" ? "Enter password" : "New password (optional)")}${ssrIncludeBooleanAttr(saving.value) ? " disabled" : ""} class="${ssrRenderClass(["flex h-10 w-full rounded-md border px-3 py-2 pr-10 text-sm text-foreground bg-background placeholder:text-muted-foreground outline-none transition disabled:opacity-50", fieldErrors.value.password ? "border-red-400 focus:ring-2 focus:ring-red-300" : "border-input focus:ring-2 focus:ring-ring/30 focus:border-ring"])}" data-v-d7861a18${_scopeId}><button type="button" class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors" data-v-d7861a18${_scopeId}>`);
								if (showPassword.value) _push(ssrRenderComponent(unref(EyeOff), { class: "h-4 w-4" }, null, _parent, _scopeId));
								else _push(ssrRenderComponent(unref(Eye), { class: "h-4 w-4" }, null, _parent, _scopeId));
								_push(`</button></div>`);
								if (fieldErrors.value.password) _push(`<p class="text-xs text-red-500 flex items-center gap-1 mt-1" data-v-d7861a18${_scopeId}><svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" data-v-d7861a18${_scopeId}><circle cx="12" cy="12" r="10" data-v-d7861a18${_scopeId}></circle><line x1="12" y1="8" x2="12" y2="12" data-v-d7861a18${_scopeId}></line><line x1="12" y1="16" x2="12.01" y2="16" data-v-d7861a18${_scopeId}></line></svg> ${ssrInterpolate(fieldErrors.value.password)}</p>`);
								else _push(`<!---->`);
								_push(`</div><div class="space-y-1.5" data-v-d7861a18${_scopeId}><label class="text-sm font-medium text-foreground" data-v-d7861a18${_scopeId}>Assign Roles</label><div class="rounded-md border border-border overflow-hidden" data-v-d7861a18${_scopeId}><div class="max-h-44 overflow-y-auto divide-y divide-border" data-v-d7861a18${_scopeId}><!--[-->`);
								ssrRenderList(roles.value, (role) => {
									_push(`<button type="button"${ssrIncludeBooleanAttr(saving.value) ? " disabled" : ""} class="w-full flex items-center justify-between px-3 py-2.5 hover:bg-muted/40 cursor-pointer transition-colors outline-none text-left disabled:opacity-50" data-v-d7861a18${_scopeId}><div class="flex items-center gap-3" data-v-d7861a18${_scopeId}>`);
									_push(ssrRenderComponent(unref(Shield), { class: "h-4 w-4 text-muted-foreground" }, null, _parent, _scopeId));
									_push(`<div data-v-d7861a18${_scopeId}><p class="font-medium text-sm text-foreground" data-v-d7861a18${_scopeId}>${ssrInterpolate(role.name)}</p><p class="text-xs text-muted-foreground" data-v-d7861a18${_scopeId}>${ssrInterpolate(role.slug)}</p></div></div>`);
									if (form.value.roleIds.includes(role.id)) _push(ssrRenderComponent(unref(Check), { class: "h-4 w-4 text-foreground" }, null, _parent, _scopeId));
									else _push(`<!---->`);
									_push(`</button>`);
								});
								_push(`<!--]--></div>`);
								if (roles.value.length === 0) _push(`<div class="p-3 text-sm text-muted-foreground" data-v-d7861a18${_scopeId}>No roles available.</div>`);
								else _push(`<!---->`);
								_push(`</div></div></div><div class="flex justify-end gap-3 px-6 py-4 border-t border-border bg-muted/30 shrink-0" data-v-d7861a18${_scopeId}>`);
								_push(ssrRenderComponent(unref(_sfc_main$3), {
									variant: "outline",
									size: "sm",
									onClick: ($event) => showDrawer.value = false,
									disabled: saving.value,
									class: "text-foreground border-border"
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Cancel`);
										else return [createTextVNode("Cancel")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(unref(_sfc_main$3), {
									size: "sm",
									onClick: saveUser,
									disabled: saving.value,
									class: "bg-primary hover:bg-primary/90 text-primary-foreground"
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) if (saving.value) _push(`<span class="flex items-center gap-2" data-v-d7861a18${_scopeId}><span class="h-3.5 w-3.5 animate-spin rounded-full border-2 border-current border-t-transparent" data-v-d7861a18${_scopeId}></span>Saving…</span>`);
										else _push(`<span data-v-d7861a18${_scopeId}>${ssrInterpolate(drawerMode.value === "create" ? "Create User" : "Save Changes")}</span>`);
										else return [saving.value ? (openBlock(), createBlock("span", {
											key: 0,
											class: "flex items-center gap-2"
										}, [createVNode("span", { class: "h-3.5 w-3.5 animate-spin rounded-full border-2 border-current border-t-transparent" }), createTextVNode("Saving…")])) : (openBlock(), createBlock("span", { key: 1 }, toDisplayString(drawerMode.value === "create" ? "Create User" : "Save Changes"), 1))];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`</div></div>`);
							} else _push(`<!---->`);
						}, "body", false, _parent);
						ssrRenderTeleport(_push, (_push) => {
							if (showDeleteDialog.value) {
								_push(`<div class="fixed inset-0 z-60 flex items-center justify-center p-4" data-v-d7861a18${_scopeId}><div class="absolute inset-0 bg-black/60 backdrop-blur-sm" data-v-d7861a18${_scopeId}></div><div class="relative z-10 w-full max-w-md bg-card border border-border rounded-xl shadow-2xl overflow-hidden" data-v-d7861a18${_scopeId}><div class="px-6 py-5 border-b border-border" data-v-d7861a18${_scopeId}><div class="flex items-start gap-4" data-v-d7861a18${_scopeId}><div class="w-10 h-10 rounded-full bg-red-100 dark:bg-red-950/40 flex items-center justify-center shrink-0" data-v-d7861a18${_scopeId}>`);
								_push(ssrRenderComponent(unref(Trash2), { class: "h-5 w-5 text-red-600 dark:text-red-400" }, null, _parent, _scopeId));
								_push(`</div><div data-v-d7861a18${_scopeId}><h3 class="font-semibold text-foreground" data-v-d7861a18${_scopeId}>Delete User</h3><p class="text-sm text-muted-foreground mt-0.5" data-v-d7861a18${_scopeId}> You are about to delete <span class="font-semibold text-foreground" data-v-d7861a18${_scopeId}>&quot;${ssrInterpolate(deletingUser.value?.fullname || deletingUser.value?.username)}&quot;</span>. This action cannot be undone. </p></div></div></div><div class="px-6 py-5 space-y-4" data-v-d7861a18${_scopeId}><p class="text-sm font-medium text-foreground" data-v-d7861a18${_scopeId}>Please enter your admin credentials to confirm:</p>`);
								if (deleteError.value) _push(`<div class="rounded-md border border-red-200 dark:border-red-900 bg-red-50 dark:bg-red-950/30 p-3" data-v-d7861a18${_scopeId}><p class="text-sm text-red-600 dark:text-red-400" data-v-d7861a18${_scopeId}>${ssrInterpolate(deleteError.value)}</p></div>`);
								else _push(`<!---->`);
								_push(`<div class="space-y-1.5" data-v-d7861a18${_scopeId}><label class="text-sm font-medium text-foreground" for="del-username" data-v-d7861a18${_scopeId}>Admin Username</label><input id="del-username"${ssrRenderAttr("value", deleteAdminUsername.value)} type="text" placeholder="Enter your admin username" class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground focus:ring-2 focus:ring-red-300 focus:border-red-400 outline-none transition" data-v-d7861a18${_scopeId}></div><div class="space-y-1.5" data-v-d7861a18${_scopeId}><label class="text-sm font-medium text-foreground" for="del-password" data-v-d7861a18${_scopeId}>Admin Password</label><div class="relative" data-v-d7861a18${_scopeId}><input id="del-password"${ssrRenderDynamicModel(showDeletePassword.value ? "text" : "password", deleteAdminPassword.value, null)}${ssrRenderAttr("type", showDeletePassword.value ? "text" : "password")} placeholder="Enter your admin password" class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 pr-10 text-sm text-foreground placeholder:text-muted-foreground focus:ring-2 focus:ring-red-300 focus:border-red-400 outline-none transition" data-v-d7861a18${_scopeId}><button type="button" class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors" data-v-d7861a18${_scopeId}>`);
								if (showDeletePassword.value) _push(ssrRenderComponent(unref(EyeOff), { class: "h-4 w-4" }, null, _parent, _scopeId));
								else _push(ssrRenderComponent(unref(Eye), { class: "h-4 w-4" }, null, _parent, _scopeId));
								_push(`</button></div></div></div><div class="flex justify-end gap-3 px-6 py-4 border-t border-border bg-muted/30" data-v-d7861a18${_scopeId}>`);
								_push(ssrRenderComponent(unref(_sfc_main$3), {
									variant: "outline",
									size: "sm",
									onClick: cancelDelete,
									class: "text-foreground border-border"
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(`Cancel`);
										else return [createTextVNode("Cancel")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(unref(_sfc_main$3), {
									size: "sm",
									onClick: confirmDelete,
									class: "bg-red-600 hover:bg-red-700 text-white"
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) {
											_push(ssrRenderComponent(unref(Trash2), { class: "h-3.5 w-3.5 mr-1.5" }, null, _parent, _scopeId));
											_push(` Delete User `);
										} else return [createVNode(unref(Trash2), { class: "h-3.5 w-3.5 mr-1.5" }), createTextVNode(" Delete User ")];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`</div></div></div>`);
							} else _push(`<!---->`);
						}, "body", false, _parent);
					} else return [
						createVNode("div", { class: "pb-6" }, [
							createVNode("div", { class: "mb-6" }, [createVNode("h1", { class: "text-xl font-bold tracking-tight text-zinc-900 dark:text-zinc-100" }, "User Management"), createVNode("p", { class: "text-xs text-zinc-500 dark:text-zinc-400 mt-0.5" }, "Manage system users, roles, and permissions.")]),
							createVNode("div", { class: "flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-5" }, [createVNode(_sfc_main$11, {
								modelValue: searchQuery.value,
								"onUpdate:modelValue": ($event) => searchQuery.value = $event,
								placeholder: "Search by name, username, phone, or role…",
								class: "w-full max-w-sm"
							}, null, 8, ["modelValue", "onUpdate:modelValue"]), createVNode("div", { class: "flex items-center gap-2 w-full sm:w-auto" }, [createVNode("div", {
								ref_key: "filterRef",
								ref: filterRef,
								class: "relative flex-1 sm:flex-none"
							}, [createVNode("button", {
								onClick: ($event) => filterOpen.value = !filterOpen.value,
								class: "w-full flex items-center justify-center gap-2 h-9 px-3 rounded-md border border-border bg-background hover:bg-accent text-foreground text-sm font-medium transition-colors"
							}, [
								(openBlock(), createBlock("svg", {
									xmlns: "http://www.w3.org/2000/svg",
									class: "h-3.5 w-3.5",
									viewBox: "0 0 24 24",
									fill: "none",
									stroke: "currentColor",
									"stroke-width": "2"
								}, [createVNode("polygon", { points: "22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3" })])),
								createVNode("span", null, "Filter"),
								activeFilterCount.value > 0 ? (openBlock(), createBlock("span", {
									key: 0,
									class: "inline-flex items-center justify-center h-4.5 min-w-[18px] px-1 rounded-full bg-primary text-primary-foreground text-[10px] font-bold leading-none"
								}, toDisplayString(activeFilterCount.value), 1)) : createCommentVNode("", true),
								createVNode(unref(ChevronDown), {
									class: ["h-3 w-3 text-muted-foreground", filterOpen.value ? "rotate-180" : ""],
									style: { "transition": "transform 0.2s" }
								}, null, 8, ["class"])
							], 8, ["onClick"]), createVNode(Transition, { name: "fade" }, {
								default: withCtx(() => [filterOpen.value ? (openBlock(), createBlock("div", {
									key: 0,
									class: "absolute left-0 sm:left-auto sm:right-0 top-full mt-1 z-30 w-[calc(100vw-2.5rem)] sm:w-64 max-w-[280px] sm:max-w-none bg-card border border-border rounded-lg shadow-xl overflow-hidden"
								}, [
									createVNode("div", { class: "flex items-center justify-between px-3 py-2.5 border-b border-border" }, [createVNode("span", { class: "text-xs font-semibold text-foreground uppercase tracking-wide" }, "Filter"), activeFilterCount.value > 0 ? (openBlock(), createBlock("button", {
										key: 0,
										onClick: clearFilters,
										class: "text-xs text-red-500 hover:text-red-600 font-medium transition-colors"
									}, "Clear all")) : createCommentVNode("", true)]),
									createVNode("div", { class: "px-3 pt-3 pb-2" }, [createVNode("p", { class: "text-[11px] font-semibold text-muted-foreground uppercase tracking-wider mb-2" }, "Role"), createVNode("div", { class: "space-y-1" }, [(openBlock(true), createBlock(Fragment, null, renderList(availableRoles.value, (role) => {
										return openBlock(), createBlock("button", {
											key: role.slug,
											onClick: ($event) => toggleRoleFilter(role.slug),
											class: "w-full flex items-center justify-between px-2 py-1.5 rounded-md hover:bg-muted/50 cursor-pointer transition-colors outline-none"
										}, [createVNode("span", { class: "text-sm font-medium text-foreground select-none" }, toDisplayString(role.name), 1), activeFilters.value.roles.includes(role.slug) ? (openBlock(), createBlock(unref(Check), {
											key: 0,
											class: "h-4 w-4 text-foreground"
										})) : createCommentVNode("", true)], 8, ["onClick"]);
									}), 128)), availableRoles.value.length === 0 ? (openBlock(), createBlock("p", {
										key: 0,
										class: "text-xs text-muted-foreground px-2 py-1"
									}, "No roles available.")) : createCommentVNode("", true)])]),
									createVNode("div", { class: "mx-3 border-t border-border" }),
									createVNode("div", { class: "px-3 pt-2 pb-3" }, [createVNode("p", { class: "text-[11px] font-semibold text-muted-foreground uppercase tracking-wider mb-2" }, "Telepon"), createVNode("div", { class: "space-y-1" }, [createVNode("button", {
										onClick: ($event) => setPhoneFilter(true),
										class: "w-full flex items-center justify-between px-2 py-1.5 rounded-md hover:bg-muted/50 cursor-pointer transition-colors outline-none"
									}, [createVNode("span", { class: "text-sm font-medium text-foreground select-none" }, "Punya Nomor Telepon"), activeFilters.value.hasPhone === true ? (openBlock(), createBlock(unref(Check), {
										key: 0,
										class: "h-4 w-4 text-foreground"
									})) : createCommentVNode("", true)], 8, ["onClick"]), createVNode("button", {
										onClick: ($event) => setPhoneFilter(false),
										class: "w-full flex items-center justify-between px-2 py-1.5 rounded-md hover:bg-muted/50 cursor-pointer transition-colors outline-none"
									}, [createVNode("span", { class: "text-sm font-medium text-foreground select-none" }, "Tanpa Nomor Telepon"), activeFilters.value.hasPhone === false ? (openBlock(), createBlock(unref(Check), {
										key: 0,
										class: "h-4 w-4 text-foreground"
									})) : createCommentVNode("", true)], 8, ["onClick"])])])
								])) : createCommentVNode("", true)]),
								_: 1
							})], 512), unref(can)("user.store") ? (openBlock(), createBlock(unref(_sfc_main$3), {
								key: 0,
								onClick: openCreate,
								size: "sm",
								class: "flex-1 sm:flex-none flex items-center justify-center gap-2 bg-primary hover:bg-primary/90 text-primary-foreground"
							}, {
								default: withCtx(() => [createVNode(unref(Plus), { class: "h-4 w-4" }), createVNode("span", null, "Add User")]),
								_: 1
							})) : createCommentVNode("", true)])]),
							createVNode(_sfc_main$9, { class: "border-border shadow-sm overflow-hidden" }, {
								default: withCtx(() => [createVNode(_sfc_main$10, { class: "p-0" }, {
									default: withCtx(() => [createVNode(_sfc_main$1, {
										data: pagedUsers.value,
										columns: columns.value,
										page: page.value,
										"page-size": pageSize.value,
										total: total.value,
										loading: loading.value,
										sortable: true,
										"empty-message": "No users found.",
										"onUpdate:page": ($event) => page.value = $event,
										"onUpdate:pageSize": ($event) => pageSize.value = $event
									}, {
										"cell-username": withCtx(({ item }) => [createVNode("div", { class: "flex items-center gap-3" }, [createVNode("div", { class: "relative shrink-0" }, [item.avatar ? (openBlock(), createBlock("img", {
											key: 0,
											src: item.avatar,
											alt: item.fullname || item.username,
											class: "w-9 h-9 rounded-full border border-border bg-muted object-cover",
											onError: (e) => {
												e.target.style.display = "none";
												e.target.nextSibling.style.display = "flex";
											}
										}, null, 40, [
											"src",
											"alt",
											"onError"
										])) : createCommentVNode("", true), createVNode("div", {
											class: ["w-9 h-9 rounded-full flex items-center justify-center text-white text-xs font-bold shrink-0", getAvatarColor(item)],
											style: item.avatar ? "display:none" : ""
										}, toDisplayString(getUserInitials(item)), 7)]), createVNode("div", { class: "flex flex-col min-w-0" }, [createVNode("span", { class: "font-semibold text-foreground text-sm truncate" }, toDisplayString(item.fullname || item.username), 1), createVNode("span", { class: "text-xs text-muted-foreground truncate" }, toDisplayString(item.username), 1)])])]),
										"cell-roles": withCtx(({ item }) => [createVNode("span", { class: ["inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[10px] font-bold uppercase tracking-wide", getRoleBadgeClass(getRoleName(item))] }, [createVNode(unref(Shield), { class: "h-3 w-3 opacity-70" }), createTextVNode(" " + toDisplayString(getRoleName(item)), 1)], 2)]),
										"cell-phone": withCtx(({ item }) => [createVNode("span", { class: "text-sm text-muted-foreground" }, toDisplayString(item.phone || "-"), 1)]),
										"cell-createdAt": withCtx(({ item }) => [createVNode("span", { class: "text-sm text-muted-foreground font-medium" }, toDisplayString(formatDate(item.createdAt)), 1)]),
										actions: withCtx(({ item }) => [createVNode(unref(_sfc_main$4), null, {
											default: withCtx(() => [createVNode(unref(_sfc_main$5), { "as-child": "" }, {
												default: withCtx(() => [createVNode("button", { class: "p-1.5 hover:bg-accent rounded-md transition-colors outline-none" }, [createVNode(unref(MoreVertical), { class: "h-4 w-4 text-muted-foreground" })])]),
												_: 1
											}), createVNode(unref(_sfc_main$6), {
												align: "end",
												class: "w-40"
											}, {
												default: withCtx(() => [
													createVNode(unref(_sfc_main$7), {
														onClick: ($event) => openEdit(item),
														class: "gap-2 cursor-pointer text-foreground"
													}, {
														default: withCtx(() => [createVNode(unref(Pencil), { class: "h-3.5 w-3.5" }), createTextVNode(" Edit ")]),
														_: 1
													}, 8, ["onClick"]),
													createVNode(unref(_sfc_main$8)),
													createVNode(unref(_sfc_main$7), {
														onClick: ($event) => openDelete(item),
														class: "gap-2 cursor-pointer text-red-600 dark:text-red-400 focus:text-red-600 dark:focus:text-red-400"
													}, {
														default: withCtx(() => [createVNode(unref(Trash2), { class: "h-3.5 w-3.5" }), createTextVNode(" Delete ")]),
														_: 1
													}, 8, ["onClick"])
												]),
												_: 2
											}, 1024)]),
											_: 2
										}, 1024)]),
										_: 1
									}, 8, [
										"data",
										"columns",
										"page",
										"page-size",
										"total",
										"loading",
										"onUpdate:page",
										"onUpdate:pageSize"
									])]),
									_: 1
								})]),
								_: 1
							})
						]),
						(openBlock(), createBlock(Teleport, { to: "body" }, [createVNode(Transition, { name: "fade" }, {
							default: withCtx(() => [showDrawer.value ? (openBlock(), createBlock("div", {
								key: 0,
								class: "fixed inset-0 z-50 bg-black/50 backdrop-blur-sm",
								onClick: ($event) => showDrawer.value = false
							}, null, 8, ["onClick"])) : createCommentVNode("", true)]),
							_: 1
						}), createVNode(Transition, { name: "slide-right" }, {
							default: withCtx(() => [showDrawer.value ? (openBlock(), createBlock("div", {
								key: 0,
								class: "fixed z-50 flex flex-col bg-card border-border shadow-2xl inset-0 sm:inset-y-0 sm:left-auto sm:right-0 sm:w-full sm:max-w-md sm:border-l"
							}, [
								createVNode("div", { class: "flex items-center justify-between px-6 py-4 border-b border-border bg-muted/30 shrink-0" }, [createVNode("div", null, [createVNode("h3", { class: "font-semibold text-lg text-foreground" }, toDisplayString(drawerMode.value === "create" ? "Add User" : "Edit User"), 1), createVNode("p", { class: "text-xs text-muted-foreground mt-0.5" }, toDisplayString(drawerMode.value === "create" ? "Create a new user account." : "Update user information."), 1)]), createVNode("button", {
									onClick: ($event) => showDrawer.value = false,
									class: "p-2 rounded-md hover:bg-accent transition-colors text-muted-foreground hover:text-foreground"
								}, [createVNode(unref(X), { class: "h-4 w-4" })], 8, ["onClick"])]),
								createVNode("div", { class: "flex-1 overflow-y-auto px-6 py-5 space-y-5" }, [
									formError.value ? (openBlock(), createBlock("div", {
										key: 0,
										class: "rounded-md border border-red-200 dark:border-red-900 bg-red-50 dark:bg-red-950/30 p-3"
									}, [createVNode("p", { class: "text-sm text-red-600 dark:text-red-400" }, toDisplayString(formError.value), 1)])) : createCommentVNode("", true),
									createVNode("div", { class: "flex items-center gap-4 p-4 rounded-lg bg-muted/50 border border-border" }, [
										createVNode("div", { class: "relative shrink-0 group" }, [avatarPreview.value ? (openBlock(), createBlock("img", {
											key: 0,
											src: avatarPreview.value,
											alt: "Avatar preview",
											class: "w-16 h-16 rounded-full object-cover border-2 border-border"
										}, null, 8, ["src"])) : (openBlock(), createBlock("div", {
											key: 1,
											class: ["w-16 h-16 rounded-full flex items-center justify-center text-white text-xl font-bold border-2 border-border", form.value.id ? getAvatarColor({ id: form.value.id }) : "bg-zinc-700"]
										}, toDisplayString(form.value.fullname || form.value.username ? getUserInitials({
											fullname: form.value.fullname,
											username: form.value.username || "?"
										}) : "?"), 3)), createVNode("button", {
											type: "button",
											onClick: ($event) => avatarInputRef.value?.click(),
											class: "absolute inset-0 rounded-full bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center"
										}, [(openBlock(), createBlock("svg", {
											xmlns: "http://www.w3.org/2000/svg",
											class: "h-5 w-5 text-white",
											viewBox: "0 0 24 24",
											fill: "none",
											stroke: "currentColor",
											"stroke-width": "2"
										}, [
											createVNode("path", { d: "M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" }),
											createVNode("polyline", { points: "17 8 12 3 7 8" }),
											createVNode("line", {
												x1: "12",
												y1: "3",
												x2: "12",
												y2: "15"
											})
										]))], 8, ["onClick"])]),
										createVNode("div", { class: "flex-1 min-w-0" }, [
											createVNode("p", { class: "font-semibold text-foreground truncate" }, toDisplayString(form.value.fullname || form.value.username || "New User"), 1),
											createVNode("p", { class: "text-xs text-muted-foreground mb-2" }, "@" + toDisplayString(form.value.username || "username"), 1),
											createVNode("div", { class: "flex items-center gap-2" }, [createVNode("button", {
												type: "button",
												onClick: ($event) => avatarInputRef.value?.click(),
												class: "text-xs font-medium px-2.5 py-1 rounded-md border border-border bg-background hover:bg-accent text-foreground transition-colors"
											}, "Upload Photo", 8, ["onClick"]), avatarPreview.value ? (openBlock(), createBlock("button", {
												key: 0,
												type: "button",
												onClick: removeAvatar,
												class: "text-xs font-medium px-2.5 py-1 rounded-md text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-950/30 transition-colors"
											}, "Remove")) : createCommentVNode("", true)])
										]),
										createVNode("input", {
											ref_key: "avatarInputRef",
											ref: avatarInputRef,
											type: "file",
											accept: "image/*",
											class: "hidden",
											onChange: handleAvatarChange
										}, null, 544)
									]),
									createVNode("div", { class: "space-y-1.5" }, [
										createVNode("label", {
											class: "text-sm font-medium text-foreground",
											for: "dw-username"
										}, [createTextVNode("Username "), createVNode("span", { class: "text-red-500" }, "*")]),
										withDirectives(createVNode("input", {
											id: "dw-username",
											"onUpdate:modelValue": ($event) => form.value.username = $event,
											type: "text",
											placeholder: "e.g. john_doe",
											disabled: saving.value,
											class: ["flex h-10 w-full rounded-md border px-3 py-2 text-sm text-foreground bg-background placeholder:text-muted-foreground outline-none transition disabled:opacity-50", fieldErrors.value.username ? "border-red-400 focus:ring-2 focus:ring-red-300" : "border-input focus:ring-2 focus:ring-ring/30 focus:border-ring"]
										}, null, 10, ["onUpdate:modelValue", "disabled"]), [[vModelText, form.value.username]]),
										fieldErrors.value.username ? (openBlock(), createBlock("p", {
											key: 0,
											class: "text-xs text-red-500 flex items-center gap-1 mt-1"
										}, [(openBlock(), createBlock("svg", {
											xmlns: "http://www.w3.org/2000/svg",
											class: "h-3 w-3 shrink-0",
											viewBox: "0 0 24 24",
											fill: "none",
											stroke: "currentColor",
											"stroke-width": "2"
										}, [
											createVNode("circle", {
												cx: "12",
												cy: "12",
												r: "10"
											}),
											createVNode("line", {
												x1: "12",
												y1: "8",
												x2: "12",
												y2: "12"
											}),
											createVNode("line", {
												x1: "12",
												y1: "16",
												x2: "12.01",
												y2: "16"
											})
										])), createTextVNode(" " + toDisplayString(fieldErrors.value.username), 1)])) : createCommentVNode("", true)
									]),
									createVNode("div", { class: "space-y-1.5" }, [createVNode("label", {
										class: "text-sm font-medium text-foreground",
										for: "dw-fullname"
									}, "Full Name"), withDirectives(createVNode("input", {
										id: "dw-fullname",
										"onUpdate:modelValue": ($event) => form.value.fullname = $event,
										type: "text",
										placeholder: "e.g. John Doe",
										disabled: saving.value,
										class: "flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground focus:ring-2 focus:ring-ring/30 focus:border-ring outline-none transition disabled:opacity-50"
									}, null, 8, ["onUpdate:modelValue", "disabled"]), [[vModelText, form.value.fullname]])]),
									createVNode("div", { class: "space-y-1.5" }, [
										createVNode("label", {
											class: "text-sm font-medium text-foreground",
											for: "dw-email"
										}, "Email"),
										withDirectives(createVNode("input", {
											id: "dw-email",
											"onUpdate:modelValue": ($event) => form.value.email = $event,
											type: "text",
											placeholder: "e.g. john@example.com",
											disabled: saving.value,
											class: ["flex h-10 w-full rounded-md border px-3 py-2 text-sm text-foreground bg-background placeholder:text-muted-foreground outline-none transition disabled:opacity-50", fieldErrors.value.email ? "border-red-400 focus:ring-2 focus:ring-red-300" : "border-input focus:ring-2 focus:ring-ring/30 focus:border-ring"]
										}, null, 10, ["onUpdate:modelValue", "disabled"]), [[vModelText, form.value.email]]),
										fieldErrors.value.email ? (openBlock(), createBlock("p", {
											key: 0,
											class: "text-xs text-red-500 flex items-center gap-1 mt-1"
										}, [(openBlock(), createBlock("svg", {
											xmlns: "http://www.w3.org/2000/svg",
											class: "h-3 w-3 shrink-0",
											viewBox: "0 0 24 24",
											fill: "none",
											stroke: "currentColor",
											"stroke-width": "2"
										}, [
											createVNode("circle", {
												cx: "12",
												cy: "12",
												r: "10"
											}),
											createVNode("line", {
												x1: "12",
												y1: "8",
												x2: "12",
												y2: "12"
											}),
											createVNode("line", {
												x1: "12",
												y1: "16",
												x2: "12.01",
												y2: "16"
											})
										])), createTextVNode(" " + toDisplayString(fieldErrors.value.email), 1)])) : createCommentVNode("", true)
									]),
									createVNode("div", { class: "space-y-1.5" }, [
										createVNode("label", {
											class: "text-sm font-medium text-foreground",
											for: "dw-phone"
										}, "Phone"),
										withDirectives(createVNode("input", {
											id: "dw-phone",
											"onUpdate:modelValue": ($event) => form.value.phone = $event,
											type: "tel",
											placeholder: "e.g. 6281234567890",
											disabled: saving.value,
											class: ["flex h-10 w-full rounded-md border px-3 py-2 text-sm text-foreground bg-background placeholder:text-muted-foreground outline-none transition disabled:opacity-50", fieldErrors.value.phone ? "border-red-400 focus:ring-2 focus:ring-red-300" : "border-input focus:ring-2 focus:ring-ring/30 focus:border-ring"]
										}, null, 10, ["onUpdate:modelValue", "disabled"]), [[vModelText, form.value.phone]]),
										fieldErrors.value.phone ? (openBlock(), createBlock("p", {
											key: 0,
											class: "text-xs text-red-500 flex items-center gap-1 mt-1"
										}, [(openBlock(), createBlock("svg", {
											xmlns: "http://www.w3.org/2000/svg",
											class: "h-3 w-3 shrink-0",
											viewBox: "0 0 24 24",
											fill: "none",
											stroke: "currentColor",
											"stroke-width": "2"
										}, [
											createVNode("circle", {
												cx: "12",
												cy: "12",
												r: "10"
											}),
											createVNode("line", {
												x1: "12",
												y1: "8",
												x2: "12",
												y2: "12"
											}),
											createVNode("line", {
												x1: "12",
												y1: "16",
												x2: "12.01",
												y2: "16"
											})
										])), createTextVNode(" " + toDisplayString(fieldErrors.value.phone), 1)])) : createCommentVNode("", true)
									]),
									createVNode("div", { class: "space-y-1.5" }, [
										createVNode("label", {
											class: "text-sm font-medium text-foreground",
											for: "dw-password"
										}, [createTextVNode(" Password "), drawerMode.value === "create" ? (openBlock(), createBlock("span", {
											key: 0,
											class: "text-red-500"
										}, "*")) : (openBlock(), createBlock("span", {
											key: 1,
											class: "text-muted-foreground text-xs font-normal"
										}, " (leave blank to keep current)"))]),
										createVNode("div", { class: "relative" }, [withDirectives(createVNode("input", {
											id: "dw-password",
											"onUpdate:modelValue": ($event) => form.value.password = $event,
											type: showPassword.value ? "text" : "password",
											placeholder: drawerMode.value === "create" ? "Enter password" : "New password (optional)",
											disabled: saving.value,
											class: ["flex h-10 w-full rounded-md border px-3 py-2 pr-10 text-sm text-foreground bg-background placeholder:text-muted-foreground outline-none transition disabled:opacity-50", fieldErrors.value.password ? "border-red-400 focus:ring-2 focus:ring-red-300" : "border-input focus:ring-2 focus:ring-ring/30 focus:border-ring"]
										}, null, 10, [
											"onUpdate:modelValue",
											"type",
											"placeholder",
											"disabled"
										]), [[vModelDynamic, form.value.password]]), createVNode("button", {
											type: "button",
											onClick: ($event) => showPassword.value = !showPassword.value,
											class: "absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
										}, [showPassword.value ? (openBlock(), createBlock(unref(EyeOff), {
											key: 0,
											class: "h-4 w-4"
										})) : (openBlock(), createBlock(unref(Eye), {
											key: 1,
											class: "h-4 w-4"
										}))], 8, ["onClick"])]),
										fieldErrors.value.password ? (openBlock(), createBlock("p", {
											key: 0,
											class: "text-xs text-red-500 flex items-center gap-1 mt-1"
										}, [(openBlock(), createBlock("svg", {
											xmlns: "http://www.w3.org/2000/svg",
											class: "h-3 w-3 shrink-0",
											viewBox: "0 0 24 24",
											fill: "none",
											stroke: "currentColor",
											"stroke-width": "2"
										}, [
											createVNode("circle", {
												cx: "12",
												cy: "12",
												r: "10"
											}),
											createVNode("line", {
												x1: "12",
												y1: "8",
												x2: "12",
												y2: "12"
											}),
											createVNode("line", {
												x1: "12",
												y1: "16",
												x2: "12.01",
												y2: "16"
											})
										])), createTextVNode(" " + toDisplayString(fieldErrors.value.password), 1)])) : createCommentVNode("", true)
									]),
									createVNode("div", { class: "space-y-1.5" }, [createVNode("label", { class: "text-sm font-medium text-foreground" }, "Assign Roles"), createVNode("div", { class: "rounded-md border border-border overflow-hidden" }, [createVNode("div", { class: "max-h-44 overflow-y-auto divide-y divide-border" }, [(openBlock(true), createBlock(Fragment, null, renderList(roles.value, (role) => {
										return openBlock(), createBlock("button", {
											key: role.id,
											type: "button",
											onClick: ($event) => toggleRole(role.id),
											disabled: saving.value,
											class: "w-full flex items-center justify-between px-3 py-2.5 hover:bg-muted/40 cursor-pointer transition-colors outline-none text-left disabled:opacity-50"
										}, [createVNode("div", { class: "flex items-center gap-3" }, [createVNode(unref(Shield), { class: "h-4 w-4 text-muted-foreground" }), createVNode("div", null, [createVNode("p", { class: "font-medium text-sm text-foreground" }, toDisplayString(role.name), 1), createVNode("p", { class: "text-xs text-muted-foreground" }, toDisplayString(role.slug), 1)])]), form.value.roleIds.includes(role.id) ? (openBlock(), createBlock(unref(Check), {
											key: 0,
											class: "h-4 w-4 text-foreground"
										})) : createCommentVNode("", true)], 8, ["onClick", "disabled"]);
									}), 128))]), roles.value.length === 0 ? (openBlock(), createBlock("div", {
										key: 0,
										class: "p-3 text-sm text-muted-foreground"
									}, "No roles available.")) : createCommentVNode("", true)])])
								]),
								createVNode("div", { class: "flex justify-end gap-3 px-6 py-4 border-t border-border bg-muted/30 shrink-0" }, [createVNode(unref(_sfc_main$3), {
									variant: "outline",
									size: "sm",
									onClick: ($event) => showDrawer.value = false,
									disabled: saving.value,
									class: "text-foreground border-border"
								}, {
									default: withCtx(() => [createTextVNode("Cancel")]),
									_: 1
								}, 8, ["onClick", "disabled"]), createVNode(unref(_sfc_main$3), {
									size: "sm",
									onClick: saveUser,
									disabled: saving.value,
									class: "bg-primary hover:bg-primary/90 text-primary-foreground"
								}, {
									default: withCtx(() => [saving.value ? (openBlock(), createBlock("span", {
										key: 0,
										class: "flex items-center gap-2"
									}, [createVNode("span", { class: "h-3.5 w-3.5 animate-spin rounded-full border-2 border-current border-t-transparent" }), createTextVNode("Saving…")])) : (openBlock(), createBlock("span", { key: 1 }, toDisplayString(drawerMode.value === "create" ? "Create User" : "Save Changes"), 1))]),
									_: 1
								}, 8, ["disabled"])])
							])) : createCommentVNode("", true)]),
							_: 1
						})])),
						(openBlock(), createBlock(Teleport, { to: "body" }, [createVNode(Transition, { name: "fade" }, {
							default: withCtx(() => [showDeleteDialog.value ? (openBlock(), createBlock("div", {
								key: 0,
								class: "fixed inset-0 z-60 flex items-center justify-center p-4"
							}, [createVNode("div", {
								class: "absolute inset-0 bg-black/60 backdrop-blur-sm",
								onClick: cancelDelete
							}), createVNode("div", { class: "relative z-10 w-full max-w-md bg-card border border-border rounded-xl shadow-2xl overflow-hidden" }, [
								createVNode("div", { class: "px-6 py-5 border-b border-border" }, [createVNode("div", { class: "flex items-start gap-4" }, [createVNode("div", { class: "w-10 h-10 rounded-full bg-red-100 dark:bg-red-950/40 flex items-center justify-center shrink-0" }, [createVNode(unref(Trash2), { class: "h-5 w-5 text-red-600 dark:text-red-400" })]), createVNode("div", null, [createVNode("h3", { class: "font-semibold text-foreground" }, "Delete User"), createVNode("p", { class: "text-sm text-muted-foreground mt-0.5" }, [
									createTextVNode(" You are about to delete "),
									createVNode("span", { class: "font-semibold text-foreground" }, "\"" + toDisplayString(deletingUser.value?.fullname || deletingUser.value?.username) + "\"", 1),
									createTextVNode(". This action cannot be undone. ")
								])])])]),
								createVNode("div", { class: "px-6 py-5 space-y-4" }, [
									createVNode("p", { class: "text-sm font-medium text-foreground" }, "Please enter your admin credentials to confirm:"),
									deleteError.value ? (openBlock(), createBlock("div", {
										key: 0,
										class: "rounded-md border border-red-200 dark:border-red-900 bg-red-50 dark:bg-red-950/30 p-3"
									}, [createVNode("p", { class: "text-sm text-red-600 dark:text-red-400" }, toDisplayString(deleteError.value), 1)])) : createCommentVNode("", true),
									createVNode("div", { class: "space-y-1.5" }, [createVNode("label", {
										class: "text-sm font-medium text-foreground",
										for: "del-username"
									}, "Admin Username"), withDirectives(createVNode("input", {
										id: "del-username",
										"onUpdate:modelValue": ($event) => deleteAdminUsername.value = $event,
										type: "text",
										placeholder: "Enter your admin username",
										class: "flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground focus:ring-2 focus:ring-red-300 focus:border-red-400 outline-none transition"
									}, null, 8, ["onUpdate:modelValue"]), [[vModelText, deleteAdminUsername.value]])]),
									createVNode("div", { class: "space-y-1.5" }, [createVNode("label", {
										class: "text-sm font-medium text-foreground",
										for: "del-password"
									}, "Admin Password"), createVNode("div", { class: "relative" }, [withDirectives(createVNode("input", {
										id: "del-password",
										"onUpdate:modelValue": ($event) => deleteAdminPassword.value = $event,
										type: showDeletePassword.value ? "text" : "password",
										placeholder: "Enter your admin password",
										class: "flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 pr-10 text-sm text-foreground placeholder:text-muted-foreground focus:ring-2 focus:ring-red-300 focus:border-red-400 outline-none transition"
									}, null, 8, ["onUpdate:modelValue", "type"]), [[vModelDynamic, deleteAdminPassword.value]]), createVNode("button", {
										type: "button",
										onClick: ($event) => showDeletePassword.value = !showDeletePassword.value,
										class: "absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
									}, [showDeletePassword.value ? (openBlock(), createBlock(unref(EyeOff), {
										key: 0,
										class: "h-4 w-4"
									})) : (openBlock(), createBlock(unref(Eye), {
										key: 1,
										class: "h-4 w-4"
									}))], 8, ["onClick"])])])
								]),
								createVNode("div", { class: "flex justify-end gap-3 px-6 py-4 border-t border-border bg-muted/30" }, [createVNode(unref(_sfc_main$3), {
									variant: "outline",
									size: "sm",
									onClick: cancelDelete,
									class: "text-foreground border-border"
								}, {
									default: withCtx(() => [createTextVNode("Cancel")]),
									_: 1
								}), createVNode(unref(_sfc_main$3), {
									size: "sm",
									onClick: confirmDelete,
									class: "bg-red-600 hover:bg-red-700 text-white"
								}, {
									default: withCtx(() => [createVNode(unref(Trash2), { class: "h-3.5 w-3.5 mr-1.5" }), createTextVNode(" Delete User ")]),
									_: 1
								})])
							])])) : createCommentVNode("", true)]),
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
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/pages/UsersPage.vue");
	return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
var UsersPage_default = /* @__PURE__ */ _plugin_vue_export_helper_default(_sfc_main, [["__scopeId", "data-v-d7861a18"]]);
//#endregion
export { UsersPage_default as default };
