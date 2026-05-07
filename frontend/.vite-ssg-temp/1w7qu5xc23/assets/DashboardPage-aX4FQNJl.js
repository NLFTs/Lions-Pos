import { n as useAuthStore, r as api } from "../main.mjs";
import { t as _plugin_vue_export_helper_default } from "./_plugin-vue_export-helper-DMwexRDj.js";
import { n as cn } from "./Button-Bj0EF1Kv.js";
import { c as _sfc_main$6, d as usePermission, t as _sfc_main$5 } from "./AppLayout-D1IhsFmL.js";
import { Fragment, computed, createBlock, createCommentVNode, createTextVNode, createVNode, mergeProps, onMounted, onUnmounted, openBlock, ref, renderList, resolveComponent, resolveDynamicComponent, toDisplayString, unref, useSSRContext, vModelText, withCtx, withDirectives, withModifiers } from "vue";
import { RouterLink } from "vue-router";
import { storeToRefs } from "pinia";
import { ssrIncludeBooleanAttr, ssrInterpolate, ssrRenderAttr, ssrRenderAttrs, ssrRenderClass, ssrRenderComponent, ssrRenderList, ssrRenderSlot, ssrRenderVNode } from "vue/server-renderer";
import { Activity, ArrowRight, Boxes, ChevronDown, ChevronRight, Clock, Eye, FileText, KeyRound, LayoutGrid, List, Loader2, Plus, Search, ShieldCheck, UserPlus, Users, Zap } from "lucide-vue-next";
//#region src/components/dashboard/StatCard.vue
var _sfc_main$4 = {
	__name: "StatCard",
	__ssrInlineRender: true,
	props: {
		label: {
			type: String,
			required: true
		},
		value: {
			type: [Number, String],
			required: true
		},
		icon: {
			type: [Object, Function],
			required: true
		},
		to: {
			type: String,
			default: null
		},
		color: {
			type: String,
			default: "text-blue-600"
		},
		bg: {
			type: String,
			default: "bg-blue-50 dark:bg-blue-950/30"
		},
		trend: {
			type: String,
			default: null
		},
		description: {
			type: String,
			default: null
		},
		horizontal: {
			type: Boolean,
			default: false
		},
		class: {
			type: String,
			default: ""
		}
	},
	emits: ["click"],
	setup(__props, { emit: __emit }) {
		const props = __props;
		const emit = __emit;
		const wrapperClass = computed(() => {
			return cn("group relative overflow-hidden rounded-xl border border-border/60 bg-card shadow-sm hover:shadow-md hover:border-primary/30 transition-all duration-200 cursor-pointer", props.horizontal ? "px-4 py-3" : "p-5", props.class);
		});
		return (_ctx, _push, _parent, _attrs) => {
			ssrRenderVNode(_push, createVNode(resolveDynamicComponent(__props.to ? "RouterLink" : "div"), mergeProps({
				to: __props.to,
				class: wrapperClass.value,
				onClick: ($event) => emit("click")
			}, _attrs), {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) {
						_push(`<div class="${ssrRenderClass([__props.bg, "absolute -top-8 -right-8 w-24 h-24 rounded-full opacity-10"])}"${_scopeId}></div>`);
						if (__props.horizontal) {
							_push(`<div class="relative flex items-center gap-4"${_scopeId}><div class="${ssrRenderClass([
								"w-10 h-10 rounded-lg flex items-center justify-center shrink-0",
								__props.bg,
								__props.color
							])}"${_scopeId}>`);
							ssrRenderVNode(_push, createVNode(resolveDynamicComponent(__props.icon), { class: "w-5 h-5" }, null), _parent, _scopeId);
							_push(`</div><div class="flex-1 min-w-0"${_scopeId}><p class="text-xs font-medium text-muted-foreground uppercase tracking-wide"${_scopeId}>${ssrInterpolate(__props.label)}</p><span class="text-xl font-bold tracking-tight"${_scopeId}>${ssrInterpolate(__props.value)}</span>`);
							if (__props.description) _push(`<p class="text-[11px] text-muted-foreground leading-relaxed mt-0.5"${_scopeId}>${ssrInterpolate(__props.description)}</p>`);
							else _push(`<!---->`);
							_push(`</div>`);
							if (__props.trend) _push(`<div class="shrink-0"${_scopeId}><span class="${ssrRenderClass([__props.trend.startsWith("+") ? "bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400" : "bg-red-50 text-red-700 dark:bg-red-950/40 dark:text-red-400", "inline-flex items-center gap-0.5 rounded-full px-2 py-0.5 text-[11px] font-semibold"])}"${_scopeId}>${ssrInterpolate(__props.trend)}</span></div>`);
							else _push(`<!---->`);
							_push(`</div>`);
						} else {
							_push(`<!--[--><div class="relative flex items-start justify-between gap-3"${_scopeId}><div class="${ssrRenderClass([
								"w-11 h-11 rounded-lg flex items-center justify-center shrink-0",
								__props.bg,
								__props.color
							])}"${_scopeId}>`);
							ssrRenderVNode(_push, createVNode(resolveDynamicComponent(__props.icon), { class: "w-5 h-5" }, null), _parent, _scopeId);
							_push(`</div>`);
							if (__props.trend) _push(`<div class="shrink-0"${_scopeId}><span class="${ssrRenderClass([__props.trend.startsWith("+") ? "bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400" : "bg-red-50 text-red-700 dark:bg-red-950/40 dark:text-red-400", "inline-flex items-center gap-0.5 rounded-full px-2 py-0.5 text-[11px] font-semibold"])}"${_scopeId}>${ssrInterpolate(__props.trend)}</span></div>`);
							else _push(`<!---->`);
							_push(`</div><div class="mt-3"${_scopeId}><span class="text-2xl font-bold tracking-tight"${_scopeId}>${ssrInterpolate(__props.value)}</span><p class="text-xs font-medium text-muted-foreground mt-0.5 uppercase tracking-wide"${_scopeId}>${ssrInterpolate(__props.label)}</p></div>`);
							if (__props.description) _push(`<p class="text-[11px] text-muted-foreground mt-2 leading-relaxed"${_scopeId}>${ssrInterpolate(__props.description)}</p>`);
							else _push(`<!---->`);
							_push(`<!--]-->`);
						}
					} else return [createVNode("div", { class: ["absolute -top-8 -right-8 w-24 h-24 rounded-full opacity-10", __props.bg] }, null, 2), __props.horizontal ? (openBlock(), createBlock("div", {
						key: 0,
						class: "relative flex items-center gap-4"
					}, [
						createVNode("div", { class: [
							"w-10 h-10 rounded-lg flex items-center justify-center shrink-0",
							__props.bg,
							__props.color
						] }, [(openBlock(), createBlock(resolveDynamicComponent(__props.icon), { class: "w-5 h-5" }))], 2),
						createVNode("div", { class: "flex-1 min-w-0" }, [
							createVNode("p", { class: "text-xs font-medium text-muted-foreground uppercase tracking-wide" }, toDisplayString(__props.label), 1),
							createVNode("span", { class: "text-xl font-bold tracking-tight" }, toDisplayString(__props.value), 1),
							__props.description ? (openBlock(), createBlock("p", {
								key: 0,
								class: "text-[11px] text-muted-foreground leading-relaxed mt-0.5"
							}, toDisplayString(__props.description), 1)) : createCommentVNode("", true)
						]),
						__props.trend ? (openBlock(), createBlock("div", {
							key: 0,
							class: "shrink-0"
						}, [createVNode("span", { class: ["inline-flex items-center gap-0.5 rounded-full px-2 py-0.5 text-[11px] font-semibold", __props.trend.startsWith("+") ? "bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400" : "bg-red-50 text-red-700 dark:bg-red-950/40 dark:text-red-400"] }, toDisplayString(__props.trend), 3)])) : createCommentVNode("", true)
					])) : (openBlock(), createBlock(Fragment, { key: 1 }, [
						createVNode("div", { class: "relative flex items-start justify-between gap-3" }, [createVNode("div", { class: [
							"w-11 h-11 rounded-lg flex items-center justify-center shrink-0",
							__props.bg,
							__props.color
						] }, [(openBlock(), createBlock(resolveDynamicComponent(__props.icon), { class: "w-5 h-5" }))], 2), __props.trend ? (openBlock(), createBlock("div", {
							key: 0,
							class: "shrink-0"
						}, [createVNode("span", { class: ["inline-flex items-center gap-0.5 rounded-full px-2 py-0.5 text-[11px] font-semibold", __props.trend.startsWith("+") ? "bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400" : "bg-red-50 text-red-700 dark:bg-red-950/40 dark:text-red-400"] }, toDisplayString(__props.trend), 3)])) : createCommentVNode("", true)]),
						createVNode("div", { class: "mt-3" }, [createVNode("span", { class: "text-2xl font-bold tracking-tight" }, toDisplayString(__props.value), 1), createVNode("p", { class: "text-xs font-medium text-muted-foreground mt-0.5 uppercase tracking-wide" }, toDisplayString(__props.label), 1)]),
						__props.description ? (openBlock(), createBlock("p", {
							key: 0,
							class: "text-[11px] text-muted-foreground mt-2 leading-relaxed"
						}, toDisplayString(__props.description), 1)) : createCommentVNode("", true)
					], 64))];
				}),
				_: 1
			}), _parent);
		};
	}
};
var _sfc_setup$4 = _sfc_main$4.setup;
_sfc_main$4.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/dashboard/StatCard.vue");
	return _sfc_setup$4 ? _sfc_setup$4(props, ctx) : void 0;
};
//#endregion
//#region src/components/dashboard/Card.vue
var _sfc_main$3 = {
	__name: "Card",
	__ssrInlineRender: true,
	props: {
		title: {
			type: String,
			required: true
		},
		subtitle: {
			type: String,
			default: ""
		},
		icon: {
			type: [Object, Function],
			default: null
		},
		actionLabel: {
			type: String,
			default: ""
		},
		actionTo: {
			type: String,
			default: ""
		},
		class: {
			type: String,
			default: ""
		}
	},
	emits: ["action-click"],
	setup(__props, { emit: __emit }) {
		const props = __props;
		const hasHeader = computed(() => props.title || props.actionLabel);
		return (_ctx, _push, _parent, _attrs) => {
			const _component_RouterLink = resolveComponent("RouterLink");
			_push(`<div${ssrRenderAttrs(mergeProps({ class: unref(cn)("rounded-xl border border-border/60 bg-card shadow-sm overflow-hidden", props.class) }, _attrs))}>`);
			if (hasHeader.value) {
				_push(`<div class="flex items-center justify-between px-5 py-3.5 border-b border-border"><div class="flex items-center gap-2">`);
				if (__props.icon) ssrRenderVNode(_push, createVNode(resolveDynamicComponent(__props.icon), { class: "w-4 h-4 text-muted-foreground" }, null), _parent);
				else _push(`<!---->`);
				_push(`<h3 class="text-sm font-semibold">${ssrInterpolate(__props.title)}</h3></div><div class="flex items-center gap-2">`);
				if (__props.subtitle) _push(`<p class="text-xs text-muted-foreground">${ssrInterpolate(__props.subtitle)}</p>`);
				else _push(`<!---->`);
				if (__props.actionLabel && __props.actionTo) _push(ssrRenderComponent(_component_RouterLink, {
					to: __props.actionTo,
					class: "inline-flex items-center gap-1 text-xs font-medium text-primary hover:text-primary/80 transition-colors"
				}, {
					default: withCtx((_, _push, _parent, _scopeId) => {
						if (_push) {
							_push(`${ssrInterpolate(__props.actionLabel)} `);
							_push(ssrRenderComponent(unref(ChevronRight), { class: "w-3.5 h-3.5" }, null, _parent, _scopeId));
						} else return [createTextVNode(toDisplayString(__props.actionLabel) + " ", 1), createVNode(unref(ChevronRight), { class: "w-3.5 h-3.5" })];
					}),
					_: 1
				}, _parent));
				else if (__props.actionLabel) {
					_push(`<button class="inline-flex items-center gap-1 text-xs font-medium text-primary hover:text-primary/80 transition-colors">${ssrInterpolate(__props.actionLabel)} `);
					_push(ssrRenderComponent(unref(ChevronRight), { class: "w-3.5 h-3.5" }, null, _parent));
					_push(`</button>`);
				} else _push(`<!---->`);
				_push(`</div></div>`);
			} else _push(`<!---->`);
			_push(`<div class="p-5">`);
			ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent);
			_push(`</div></div>`);
		};
	}
};
var _sfc_setup$3 = _sfc_main$3.setup;
_sfc_main$3.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/dashboard/Card.vue");
	return _sfc_setup$3 ? _sfc_setup$3(props, ctx) : void 0;
};
//#endregion
//#region src/components/dashboard/ActivityTimeline.vue
var defaultColor = "text-zinc-500 dark:text-zinc-400";
var _sfc_main$2 = {
	__name: "ActivityTimeline",
	__ssrInlineRender: true,
	props: {
		items: {
			type: Array,
			required: true
		},
		class: {
			type: String,
			default: ""
		}
	},
	setup(__props) {
		const props = __props;
		return (_ctx, _push, _parent, _attrs) => {
			_push(`<div${ssrRenderAttrs(mergeProps({ class: unref(cn)("space-y-0", props.class) }, _attrs))}><!--[-->`);
			ssrRenderList(__props.items, (item, index) => {
				_push(`<div class="flex gap-3 group"><div class="flex flex-col items-center shrink-0"><div class="${ssrRenderClass([item.color || defaultColor, "w-8 h-8 rounded-full flex items-center justify-center shrink-0 transition-colors"])}">`);
				if (item.icon) ssrRenderVNode(_push, createVNode(resolveDynamicComponent(item.icon), { class: "w-4 h-4" }, null), _parent);
				else if (item.initials) _push(`<span class="text-xs font-bold">${ssrInterpolate(item.initials)}</span>`);
				else _push(`<!---->`);
				_push(`</div>`);
				if (index < __props.items.length - 1) _push(`<div class="w-px flex-1 bg-border group-last:hidden"></div>`);
				else _push(`<!---->`);
				_push(`</div><div class="flex-1 min-w-0 py-0.5"><div class="flex items-start justify-between gap-2"><p class="text-sm font-medium leading-snug">${ssrInterpolate(item.title)}</p>`);
				if (item.time) _push(`<span class="text-[11px] text-muted-foreground shrink-0 mt-0.5">${ssrInterpolate(item.time)}</span>`);
				else _push(`<!---->`);
				_push(`</div>`);
				if (item.description) _push(`<p class="text-xs text-muted-foreground mt-0.5 leading-relaxed">${ssrInterpolate(item.description)}</p>`);
				else _push(`<!---->`);
				_push(`</div></div>`);
			});
			_push(`<!--]-->`);
			if (__props.items.length === 0) _push(`<div class="py-8 text-center"><p class="text-sm text-muted-foreground">Belum ada aktivitas.</p></div>`);
			else _push(`<!---->`);
			_push(`</div>`);
		};
	}
};
var _sfc_setup$2 = _sfc_main$2.setup;
_sfc_main$2.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/dashboard/ActivityTimeline.vue");
	return _sfc_setup$2 ? _sfc_setup$2(props, ctx) : void 0;
};
//#endregion
//#region src/components/dashboard/MobileSummaryTabs.vue
var _sfc_main$1 = {
	__name: "MobileSummaryTabs",
	__ssrInlineRender: true,
	props: {
		recentPosts: {
			type: Array,
			default: () => []
		},
		recentUsers: {
			type: Array,
			default: () => []
		},
		recentActivities: {
			type: Array,
			default: () => []
		},
		formatDate: {
			type: Function,
			required: true
		}
	},
	setup(__props) {
		const tabs = [
			{
				key: "posts",
				label: "Post Terbaru",
				icon: FileText,
				actionTo: "/dashboard/posts",
				actionLabel: "Lihat Semua"
			},
			{
				key: "users",
				label: "User Terbaru",
				icon: UserPlus,
				actionTo: "/dashboard/users",
				actionLabel: "Lihat Semua"
			},
			{
				key: "activities",
				label: "Aktivitas",
				icon: Activity,
				actionTo: null,
				actionLabel: ""
			}
		];
		const activeTab = ref("posts");
		const activeTabData = computed(() => tabs.find((t) => t.key === activeTab.value));
		return (_ctx, _push, _parent, _attrs) => {
			_push(`<div${ssrRenderAttrs(mergeProps({ class: "lg:hidden rounded-xl border border-border/60 bg-card shadow-sm overflow-hidden" }, _attrs))} data-v-d2e9636a><div class="flex border-b border-border" data-v-d2e9636a><!--[-->`);
			ssrRenderList(tabs, (tab) => {
				_push(`<button class="${ssrRenderClass([activeTab.value === tab.key ? "border-primary text-primary bg-primary/5" : "border-transparent text-muted-foreground hover:text-foreground hover:bg-muted/20", "flex-1 flex items-center justify-center gap-1.5 py-3 text-xs font-medium border-b-2 transition-colors duration-150"])}" data-v-d2e9636a>`);
				ssrRenderVNode(_push, createVNode(resolveDynamicComponent(tab.icon), { class: "w-3.5 h-3.5 shrink-0" }, null), _parent);
				_push(`<span class="truncate" data-v-d2e9636a>${ssrInterpolate(tab.label)}</span></button>`);
			});
			_push(`<!--]--></div>`);
			if (activeTabData.value?.actionTo) {
				_push(`<div class="flex items-center justify-between px-4 py-2 bg-muted/20 border-b border-border/40" data-v-d2e9636a><span class="text-xs text-muted-foreground" data-v-d2e9636a>${ssrInterpolate(activeTab.value === "posts" ? `${__props.recentPosts.length} post ditampilkan` : activeTab.value === "users" ? `${__props.recentUsers.length} user ditampilkan` : "")}</span>`);
				_push(ssrRenderComponent(unref(RouterLink), {
					to: activeTabData.value.actionTo,
					class: "inline-flex items-center gap-1 text-xs font-medium text-primary hover:text-primary/80 transition-colors"
				}, {
					default: withCtx((_, _push, _parent, _scopeId) => {
						if (_push) {
							_push(`${ssrInterpolate(activeTabData.value.actionLabel)} `);
							_push(ssrRenderComponent(unref(ChevronRight), { class: "w-3.5 h-3.5" }, null, _parent, _scopeId));
						} else return [createTextVNode(toDisplayString(activeTabData.value.actionLabel) + " ", 1), createVNode(unref(ChevronRight), { class: "w-3.5 h-3.5" })];
					}),
					_: 1
				}, _parent));
				_push(`</div>`);
			} else _push(`<!---->`);
			_push(`<div class="p-4" data-v-d2e9636a><div class="${ssrRenderClass(["tab-panel", { "tab-panel--active": activeTab.value === "posts" }])}" data-v-d2e9636a>`);
			if (__props.recentPosts.length > 0) {
				_push(`<div class="space-y-2" data-v-d2e9636a><!--[-->`);
				ssrRenderList(__props.recentPosts, (post) => {
					_push(ssrRenderComponent(unref(RouterLink), {
						key: post.id,
						to: "/dashboard/posts",
						class: "flex items-center justify-between p-3 rounded-lg border border-border/40 hover:bg-muted/30 hover:border-primary/20 transition-all group"
					}, {
						default: withCtx((_, _push, _parent, _scopeId) => {
							if (_push) {
								_push(`<div class="flex-1 min-w-0" data-v-d2e9636a${_scopeId}><p class="text-sm font-medium truncate group-hover:text-primary transition-colors" data-v-d2e9636a${_scopeId}>${ssrInterpolate(post.title)}</p><div class="flex items-center gap-2 mt-1" data-v-d2e9636a${_scopeId}><span class="${ssrRenderClass([post.status === "published" ? "bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400" : "bg-amber-50 text-amber-700 dark:bg-amber-950/40 dark:text-amber-400", "inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-semibold"])}" data-v-d2e9636a${_scopeId}>${ssrInterpolate(post.status === "published" ? "Published" : "Draft")}</span><span class="text-xs text-muted-foreground" data-v-d2e9636a${_scopeId}>${ssrInterpolate(__props.formatDate(post.createdAt))}</span></div></div>`);
								_push(ssrRenderComponent(unref(ArrowRight), { class: "w-4 h-4 text-muted-foreground group-hover:text-primary group-hover:translate-x-0.5 transition-all shrink-0 ml-2" }, null, _parent, _scopeId));
							} else return [createVNode("div", { class: "flex-1 min-w-0" }, [createVNode("p", { class: "text-sm font-medium truncate group-hover:text-primary transition-colors" }, toDisplayString(post.title), 1), createVNode("div", { class: "flex items-center gap-2 mt-1" }, [createVNode("span", { class: ["inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-semibold", post.status === "published" ? "bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400" : "bg-amber-50 text-amber-700 dark:bg-amber-950/40 dark:text-amber-400"] }, toDisplayString(post.status === "published" ? "Published" : "Draft"), 3), createVNode("span", { class: "text-xs text-muted-foreground" }, toDisplayString(__props.formatDate(post.createdAt)), 1)])]), createVNode(unref(ArrowRight), { class: "w-4 h-4 text-muted-foreground group-hover:text-primary group-hover:translate-x-0.5 transition-all shrink-0 ml-2" })];
						}),
						_: 2
					}, _parent));
				});
				_push(`<!--]--></div>`);
			} else {
				_push(`<div class="py-10 text-center" data-v-d2e9636a>`);
				_push(ssrRenderComponent(unref(FileText), { class: "w-8 h-8 mx-auto text-muted-foreground/40 mb-2" }, null, _parent));
				_push(`<p class="text-sm text-muted-foreground" data-v-d2e9636a>Belum ada post.</p></div>`);
			}
			_push(`</div><div class="${ssrRenderClass(["tab-panel", { "tab-panel--active": activeTab.value === "users" }])}" data-v-d2e9636a>`);
			if (__props.recentUsers.length > 0) {
				_push(`<div class="space-y-2" data-v-d2e9636a><!--[-->`);
				ssrRenderList(__props.recentUsers, (u) => {
					_push(`<div class="flex items-center gap-3 p-3 rounded-lg border border-border/40 hover:bg-muted/30 hover:border-primary/20 transition-all" data-v-d2e9636a><div class="w-9 h-9 rounded-full bg-primary/10 flex items-center justify-center text-primary text-sm font-bold shrink-0" data-v-d2e9636a>${ssrInterpolate((u.fullname || u.username).charAt(0).toUpperCase())}</div><div class="flex-1 min-w-0" data-v-d2e9636a><p class="text-sm font-medium truncate" data-v-d2e9636a>${ssrInterpolate(u.fullname || u.username)}</p><p class="text-xs text-muted-foreground" data-v-d2e9636a>@${ssrInterpolate(u.username)}</p></div></div>`);
				});
				_push(`<!--]--></div>`);
			} else {
				_push(`<div class="py-10 text-center" data-v-d2e9636a>`);
				_push(ssrRenderComponent(unref(Users), { class: "w-8 h-8 mx-auto text-muted-foreground/40 mb-2" }, null, _parent));
				_push(`<p class="text-sm text-muted-foreground" data-v-d2e9636a>Belum ada user.</p></div>`);
			}
			_push(`</div><div class="${ssrRenderClass(["tab-panel", { "tab-panel--active": activeTab.value === "activities" }])}" data-v-d2e9636a>`);
			if (__props.recentActivities.length > 0) {
				_push(`<div class="space-y-3" data-v-d2e9636a><!--[-->`);
				ssrRenderList(__props.recentActivities, (item, i) => {
					_push(`<div class="flex items-start gap-3" data-v-d2e9636a><div class="${ssrRenderClass([item.color, "w-8 h-8 rounded-full flex items-center justify-center shrink-0 mt-0.5"])}" data-v-d2e9636a>`);
					ssrRenderVNode(_push, createVNode(resolveDynamicComponent(item.icon), { class: "w-4 h-4" }, null), _parent);
					_push(`</div><div class="flex-1 min-w-0" data-v-d2e9636a><p class="text-sm font-medium truncate" data-v-d2e9636a>${ssrInterpolate(item.title)}</p><p class="text-xs text-muted-foreground" data-v-d2e9636a>${ssrInterpolate(item.description)}</p></div><span class="text-[11px] text-muted-foreground shrink-0 mt-0.5" data-v-d2e9636a>${ssrInterpolate(item.time)}</span></div>`);
				});
				_push(`<!--]--></div>`);
			} else {
				_push(`<div class="py-10 text-center" data-v-d2e9636a>`);
				_push(ssrRenderComponent(unref(Activity), { class: "w-8 h-8 mx-auto text-muted-foreground/40 mb-2" }, null, _parent));
				_push(`<p class="text-sm text-muted-foreground" data-v-d2e9636a>Belum ada aktivitas.</p></div>`);
			}
			_push(`</div></div></div>`);
		};
	}
};
var _sfc_setup$1 = _sfc_main$1.setup;
_sfc_main$1.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/dashboard/MobileSummaryTabs.vue");
	return _sfc_setup$1 ? _sfc_setup$1(props, ctx) : void 0;
};
var MobileSummaryTabs_default = /* @__PURE__ */ _plugin_vue_export_helper_default(_sfc_main$1, [["__scopeId", "data-v-d2e9636a"]]);
//#endregion
//#region src/pages/DashboardPage.vue
var _sfc_main = {
	__name: "DashboardPage",
	__ssrInlineRender: true,
	setup(__props) {
		const { user } = storeToRefs(useAuthStore());
		const { can } = usePermission();
		const loading = ref(true);
		const error = ref(null);
		const lastRefresh = ref(null);
		const searchQuery = ref("");
		const statsViewMode = ref("grid");
		const isAboutModalOpen = ref(false);
		const dropdownOpen = ref(false);
		const dropdownRef = ref(null);
		function handleOutsideClick(e) {
			if (dropdownRef.value && !dropdownRef.value.contains(e.target)) dropdownOpen.value = false;
		}
		onMounted(() => {
			fetchStats();
			document.addEventListener("click", handleOutsideClick);
		});
		onUnmounted(() => document.removeEventListener("click", handleOutsideClick));
		const stats = ref({
			totalUsers: 0,
			totalPosts: 0,
			totalCategories: 0,
			totalRoles: 0,
			totalPermissions: 0,
			totalModules: 0,
			publishedPosts: 0,
			draftPosts: 0
		});
		const recentUsers = ref([]);
		const recentPosts = ref([]);
		const recentActivities = ref([]);
		async function fetchStats() {
			loading.value = true;
			error.value = null;
			try {
				const [usersRes, postsRes, categoriesRes, rolesRes, permsRes, modulesRes] = await Promise.allSettled([
					api.get("/api/v1/users"),
					api.get("/api/v1/posts?page=0&size=5"),
					api.get("/api/v1/categories"),
					api.get("/api/v1/roles"),
					api.get("/api/v1/permissions"),
					api.get("/api/v1/modules")
				]);
				if (usersRes.status === "fulfilled") {
					const users = usersRes.value.data.data;
					stats.value.totalUsers = Array.isArray(users) ? users.length : 0;
					recentUsers.value = Array.isArray(users) ? users.slice(0, 5) : [];
				}
				if (postsRes.status === "fulfilled") {
					const postsData = postsRes.value.data.data;
					stats.value.totalPosts = postsData?.totalElements ?? 0;
					stats.value.publishedPosts = postsData?.content?.filter((p) => p.status === "published").length ?? 0;
					stats.value.draftPosts = postsData?.content?.filter((p) => p.status === "draft").length ?? 0;
					recentPosts.value = postsData?.content?.slice(0, 5) ?? [];
				}
				if (categoriesRes.status === "fulfilled") {
					const cats = categoriesRes.value.data.data;
					stats.value.totalCategories = Array.isArray(cats) ? cats.length : 0;
				}
				if (rolesRes.status === "fulfilled") {
					const roles = rolesRes.value.data.data;
					stats.value.totalRoles = Array.isArray(roles) ? roles.length : 0;
				}
				if (permsRes.status === "fulfilled") {
					const perms = permsRes.value.data.data;
					stats.value.totalPermissions = Array.isArray(perms) ? perms.length : 0;
				}
				if (modulesRes.status === "fulfilled") {
					const mods = modulesRes.value.data.data;
					stats.value.totalModules = Array.isArray(mods) ? mods.length : 0;
				}
				generateRecentActivities();
				lastRefresh.value = /* @__PURE__ */ new Date();
			} catch (err) {
				error.value = err.response?.data?.message || "Gagal memuat dashboard.";
			} finally {
				loading.value = false;
			}
		}
		function generateRecentActivities() {
			const activities = [];
			recentUsers.value.slice(0, 3).forEach((u) => {
				activities.push({
					title: `User baru: ${u.fullname || u.username}`,
					description: `@${u.username} bergabung`,
					icon: UserPlus,
					color: "bg-blue-50 text-blue-600 dark:bg-blue-950/40 dark:text-blue-400",
					time: formatDate(u.createdAt),
					initials: (u.fullname || u.username).charAt(0).toUpperCase()
				});
			});
			recentPosts.value.slice(0, 2).forEach((p) => {
				activities.push({
					title: `Post: ${p.title}`,
					description: `Status: ${p.status === "published" ? "Published" : "Draft"}`,
					icon: FileText,
					color: "bg-emerald-50 text-emerald-600 dark:bg-emerald-950/40 dark:text-emerald-400",
					time: formatDate(p.createdAt)
				});
			});
			recentActivities.value = activities.slice(0, 5);
		}
		const statCards = computed(() => {
			const cards = [];
			if (can("user.index")) cards.push({
				label: "Total User",
				value: stats.value.totalUsers,
				icon: Users,
				to: "/dashboard/users",
				color: "text-blue-600",
				bg: "bg-blue-50 dark:bg-blue-950/30",
				trend: "+3",
				description: `${recentUsers.value.length} terbaru ditampilkan`
			});
			if (can("produk.index")) cards.push({
				label: "Total Post",
				value: stats.value.totalPosts,
				icon: FileText,
				to: "/dashboard/posts",
				color: "text-emerald-600",
				bg: "bg-emerald-50 dark:bg-emerald-950/30",
				trend: `${stats.value.publishedPosts} published`,
				description: `${stats.value.draftPosts} draft menunggu`
			});
			if (can("category.index")) cards.push({
				label: "Kategori",
				value: stats.value.totalCategories,
				icon: Boxes,
				to: "/dashboard/categories",
				color: "text-violet-600",
				bg: "bg-violet-50 dark:bg-violet-950/30"
			});
			if (can("role.index")) cards.push({
				label: "Role",
				value: stats.value.totalRoles,
				icon: ShieldCheck,
				to: "/dashboard/roles",
				color: "text-amber-600",
				bg: "bg-amber-50 dark:bg-amber-950/30"
			});
			if (can("permission.index")) cards.push({
				label: "Permission",
				value: stats.value.totalPermissions,
				icon: KeyRound,
				to: "/dashboard/permissions",
				color: "text-rose-600",
				bg: "bg-rose-50 dark:bg-rose-950/30"
			});
			if (can("module.index")) cards.push({
				label: "Modul",
				value: stats.value.totalModules,
				icon: Zap,
				to: "/dashboard/modules",
				color: "text-cyan-600",
				bg: "bg-cyan-50 dark:bg-cyan-950/30"
			});
			return cards;
		});
		computed(() => can("produk.store"));
		const createMenuItems = computed(() => {
			const items = [];
			if (can("user.store")) items.push({
				label: "User",
				icon: Users,
				to: "/dashboard/users"
			});
			if (can("produk.store")) items.push({
				label: "Post",
				icon: FileText,
				to: "/dashboard/posts"
			});
			if (can("category.store")) items.push({
				label: "Kategori",
				icon: Boxes,
				to: "/dashboard/categories"
			});
			if (can("role.store")) items.push({
				label: "Role",
				icon: ShieldCheck,
				to: "/dashboard/roles"
			});
			if (can("permission.store")) items.push({
				label: "Permission",
				icon: KeyRound,
				to: "/dashboard/permissions"
			});
			if (can("module.store")) items.push({
				label: "Modul",
				icon: Zap,
				to: "/dashboard/modules"
			});
			return items;
		});
		const filteredStatCards = computed(() => {
			const q = searchQuery.value.trim().toLowerCase();
			if (!q) return statCards.value;
			return statCards.value.filter((c) => c.label.toLowerCase().includes(q));
		});
		function formatDate(dt) {
			if (!dt) return "-";
			const date = new Date(dt);
			const diff = /* @__PURE__ */ new Date() - date;
			const minutes = Math.floor(diff / 6e4);
			const hours = Math.floor(diff / 36e5);
			const days = Math.floor(diff / 864e5);
			if (minutes < 1) return "Baru saja";
			if (minutes < 60) return `${minutes} menit lalu`;
			if (hours < 24) return `${hours} jam lalu`;
			if (days < 7) return `${days} hari lalu`;
			return date.toLocaleDateString("id-ID", {
				day: "2-digit",
				month: "short",
				year: "numeric"
			});
		}
		function formatTime(dt) {
			if (!dt) return "-";
			return new Date(dt).toLocaleTimeString("id-ID", {
				hour: "2-digit",
				minute: "2-digit"
			});
		}
		return (_ctx, _push, _parent, _attrs) => {
			const _component_RouterLink = resolveComponent("RouterLink");
			_push(ssrRenderComponent(_sfc_main$5, _attrs, {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) {
						_push(`<div class="space-y-6"${_scopeId}><div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between"${_scopeId}><div${_scopeId}><h1 class="text-2xl font-bold tracking-tight"${_scopeId}>Selamat datang, ${ssrInterpolate(unref(user)?.fullname || unref(user)?.username)}! 👋</h1><div class="flex items-center gap-2 mt-1"${_scopeId}><p class="text-sm text-muted-foreground"${_scopeId}> Berikut ringkasan data dan aktivitas aplikasi hari ini. </p><span class="text-zinc-300 dark:text-zinc-700"${_scopeId}>•</span><button class="text-xs font-semibold text-primary hover:underline transition-all"${_scopeId}> Tentang Kami </button></div></div><div class="flex items-center gap-2"${_scopeId}><span class="text-xs text-muted-foreground"${_scopeId}>`);
						_push(ssrRenderComponent(unref(Clock), { class: "w-3 h-3 inline mr-1" }, null, _parent, _scopeId));
						_push(` Terakhir diperbarui: ${ssrInterpolate(lastRefresh.value ? formatTime(lastRefresh.value) : "-")}</span><button${ssrIncludeBooleanAttr(loading.value) ? " disabled" : ""} class="inline-flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-xs font-medium bg-primary text-primary-foreground hover:bg-primary/90 transition-colors disabled:opacity-50"${_scopeId}>`);
						if (loading.value) _push(ssrRenderComponent(unref(Loader2), { class: "w-3.5 h-3.5 animate-spin" }, null, _parent, _scopeId));
						else _push(ssrRenderComponent(unref(Eye), { class: "w-3.5 h-3.5" }, null, _parent, _scopeId));
						_push(` Refresh </button></div></div><div class="flex items-center gap-2"${_scopeId}><div class="relative flex-1"${_scopeId}>`);
						_push(ssrRenderComponent(unref(Search), { class: "absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground pointer-events-none" }, null, _parent, _scopeId));
						_push(`<input${ssrRenderAttr("value", searchQuery.value)} type="text" placeholder="Cari statistik &amp; data..." class="w-full rounded-lg border border-border bg-card pl-9 pr-4 py-2 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary transition-all"${_scopeId}></div><div class="flex items-center gap-1 rounded-lg border border-border bg-card p-1 shrink-0"${_scopeId}><button class="${ssrRenderClass([statsViewMode.value === "grid" ? "bg-primary text-primary-foreground" : "text-muted-foreground hover:text-foreground", "rounded-md p-1.5 transition-colors"])}" title="Tampilan kotak"${_scopeId}>`);
						_push(ssrRenderComponent(unref(LayoutGrid), { class: "w-4 h-4" }, null, _parent, _scopeId));
						_push(`</button><button class="${ssrRenderClass([statsViewMode.value === "list" ? "bg-primary text-primary-foreground" : "text-muted-foreground hover:text-foreground", "rounded-md p-1.5 transition-colors"])}" title="Tampilan daftar"${_scopeId}>`);
						_push(ssrRenderComponent(unref(List), { class: "w-4 h-4" }, null, _parent, _scopeId));
						_push(`</button></div><div class="relative shrink-0"${_scopeId}><button class="inline-flex items-center gap-2 rounded-lg px-3.5 py-2 text-sm font-medium bg-primary text-primary-foreground hover:bg-primary/90 transition-all shadow-sm hover:shadow"${_scopeId}>`);
						_push(ssrRenderComponent(unref(Plus), { class: "w-4 h-4" }, null, _parent, _scopeId));
						_push(` Buat Baru `);
						_push(ssrRenderComponent(unref(ChevronDown), { class: ["w-4 h-4 transition-transform duration-200", dropdownOpen.value ? "rotate-180" : ""] }, null, _parent, _scopeId));
						_push(`</button>`);
						if (dropdownOpen.value) {
							_push(`<div class="absolute right-0 top-full mt-1.5 w-44 rounded-xl border border-border bg-card shadow-lg z-50 overflow-hidden py-1"${_scopeId}><!--[-->`);
							ssrRenderList(createMenuItems.value, (item) => {
								_push(ssrRenderComponent(_component_RouterLink, {
									key: item.label,
									to: item.to,
									onClick: ($event) => dropdownOpen.value = false,
									class: "flex items-center gap-2.5 px-3.5 py-2.5 text-sm hover:bg-muted/60 transition-colors"
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) {
											ssrRenderVNode(_push, createVNode(resolveDynamicComponent(item.icon), { class: "w-4 h-4 text-muted-foreground" }, null), _parent, _scopeId);
											_push(` ${ssrInterpolate(item.label)}`);
										} else return [(openBlock(), createBlock(resolveDynamicComponent(item.icon), { class: "w-4 h-4 text-muted-foreground" })), createTextVNode(" " + toDisplayString(item.label), 1)];
									}),
									_: 2
								}, _parent, _scopeId));
							});
							_push(`<!--]-->`);
							if (createMenuItems.value.length === 0) _push(`<div class="px-4 py-3 text-xs text-muted-foreground"${_scopeId}> Tidak ada akses buat. </div>`);
							else _push(`<!---->`);
							_push(`</div>`);
						} else _push(`<!---->`);
						_push(`</div></div>`);
						if (loading.value) {
							_push(`<div class="flex items-center justify-center py-20"${_scopeId}><div class="flex flex-col items-center gap-3"${_scopeId}>`);
							_push(ssrRenderComponent(unref(Loader2), { class: "w-8 h-8 animate-spin text-primary" }, null, _parent, _scopeId));
							_push(`<p class="text-sm text-muted-foreground"${_scopeId}>Memuat data dashboard...</p></div></div>`);
						} else if (error.value) {
							_push(`<div class="rounded-xl border border-destructive/30 bg-destructive/5 px-5 py-4"${_scopeId}><div class="flex items-start gap-3"${_scopeId}>`);
							_push(ssrRenderComponent(unref(Activity), { class: "w-5 h-5 text-destructive shrink-0 mt-0.5" }, null, _parent, _scopeId));
							_push(`<div${_scopeId}><p class="text-sm font-medium text-destructive"${_scopeId}>${ssrInterpolate(error.value)}</p><p class="text-xs text-muted-foreground mt-1"${_scopeId}>Silakan coba refresh halaman.</p></div></div></div>`);
						} else {
							_push(`<!--[-->`);
							if (!searchQuery.value) _push(ssrRenderComponent(MobileSummaryTabs_default, {
								"recent-posts": recentPosts.value,
								"recent-users": recentUsers.value,
								"recent-activities": recentActivities.value,
								"format-date": formatDate
							}, null, _parent, _scopeId));
							else _push(`<!---->`);
							if (filteredStatCards.value.length > 0) {
								_push(`<div class="${ssrRenderClass([statsViewMode.value === "grid" ? "grid grid-cols-2 gap-3" : "flex flex-col gap-2", "lg:hidden"])}"${_scopeId}><!--[-->`);
								ssrRenderList(filteredStatCards.value, (card) => {
									_push(ssrRenderComponent(_sfc_main$4, mergeProps({ key: card.label }, { ref_for: true }, card, { horizontal: statsViewMode.value === "list" }), null, _parent, _scopeId));
								});
								_push(`<!--]--></div>`);
							} else if (searchQuery.value && filteredStatCards.value.length === 0) _push(`<div class="lg:hidden py-6 text-center text-sm text-muted-foreground"${_scopeId}> Tidak ada statistik yang cocok dengan &quot;${ssrInterpolate(searchQuery.value)}&quot; </div>`);
							else _push(`<!---->`);
							if (!searchQuery.value) {
								_push(`<div class="hidden lg:grid grid-cols-1 lg:grid-cols-3 gap-6"${_scopeId}><div class="space-y-6 lg:col-span-1"${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$3, {
									title: "Post Terbaru",
									subtitle: "5 post terakhir",
									icon: unref(FileText),
									"action-label": "Lihat Semua",
									"action-to": "/dashboard/posts"
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) if (recentPosts.value.length > 0) {
											_push(`<div class="space-y-3"${_scopeId}><!--[-->`);
											ssrRenderList(recentPosts.value, (post) => {
												_push(ssrRenderComponent(_component_RouterLink, {
													key: post.id,
													to: `/dashboard/posts`,
													class: "flex items-center justify-between p-3 rounded-lg border border-border/40 hover:bg-muted/30 hover:border-primary/20 transition-all group"
												}, {
													default: withCtx((_, _push, _parent, _scopeId) => {
														if (_push) {
															_push(`<div class="flex-1 min-w-0"${_scopeId}><p class="text-sm font-medium truncate group-hover:text-primary transition-colors"${_scopeId}>${ssrInterpolate(post.title)}</p><div class="flex items-center gap-2 mt-1"${_scopeId}><span class="${ssrRenderClass([post.status === "published" ? "bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400" : "bg-amber-50 text-amber-700 dark:bg-amber-950/40 dark:text-amber-400", "inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-semibold"])}"${_scopeId}>${ssrInterpolate(post.status === "published" ? "Published" : "Draft")}</span><span class="text-xs text-muted-foreground"${_scopeId}>${ssrInterpolate(formatDate(post.createdAt))}</span></div></div>`);
															_push(ssrRenderComponent(unref(ArrowRight), { class: "w-4 h-4 text-muted-foreground group-hover:text-primary group-hover:translate-x-0.5 transition-all" }, null, _parent, _scopeId));
														} else return [createVNode("div", { class: "flex-1 min-w-0" }, [createVNode("p", { class: "text-sm font-medium truncate group-hover:text-primary transition-colors" }, toDisplayString(post.title), 1), createVNode("div", { class: "flex items-center gap-2 mt-1" }, [createVNode("span", { class: ["inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-semibold", post.status === "published" ? "bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400" : "bg-amber-50 text-amber-700 dark:bg-amber-950/40 dark:text-amber-400"] }, toDisplayString(post.status === "published" ? "Published" : "Draft"), 3), createVNode("span", { class: "text-xs text-muted-foreground" }, toDisplayString(formatDate(post.createdAt)), 1)])]), createVNode(unref(ArrowRight), { class: "w-4 h-4 text-muted-foreground group-hover:text-primary group-hover:translate-x-0.5 transition-all" })];
													}),
													_: 2
												}, _parent, _scopeId));
											});
											_push(`<!--]--></div>`);
										} else {
											_push(`<div class="py-8 text-center"${_scopeId}>`);
											_push(ssrRenderComponent(unref(FileText), { class: "w-8 h-8 mx-auto text-muted-foreground/40 mb-2" }, null, _parent, _scopeId));
											_push(`<p class="text-sm text-muted-foreground"${_scopeId}>Belum ada post.</p></div>`);
										}
										else return [recentPosts.value.length > 0 ? (openBlock(), createBlock("div", {
											key: 0,
											class: "space-y-3"
										}, [(openBlock(true), createBlock(Fragment, null, renderList(recentPosts.value, (post) => {
											return openBlock(), createBlock(_component_RouterLink, {
												key: post.id,
												to: `/dashboard/posts`,
												class: "flex items-center justify-between p-3 rounded-lg border border-border/40 hover:bg-muted/30 hover:border-primary/20 transition-all group"
											}, {
												default: withCtx(() => [createVNode("div", { class: "flex-1 min-w-0" }, [createVNode("p", { class: "text-sm font-medium truncate group-hover:text-primary transition-colors" }, toDisplayString(post.title), 1), createVNode("div", { class: "flex items-center gap-2 mt-1" }, [createVNode("span", { class: ["inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-semibold", post.status === "published" ? "bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400" : "bg-amber-50 text-amber-700 dark:bg-amber-950/40 dark:text-amber-400"] }, toDisplayString(post.status === "published" ? "Published" : "Draft"), 3), createVNode("span", { class: "text-xs text-muted-foreground" }, toDisplayString(formatDate(post.createdAt)), 1)])]), createVNode(unref(ArrowRight), { class: "w-4 h-4 text-muted-foreground group-hover:text-primary group-hover:translate-x-0.5 transition-all" })]),
												_: 2
											}, 1024);
										}), 128))])) : (openBlock(), createBlock("div", {
											key: 1,
											class: "py-8 text-center"
										}, [createVNode(unref(FileText), { class: "w-8 h-8 mx-auto text-muted-foreground/40 mb-2" }), createVNode("p", { class: "text-sm text-muted-foreground" }, "Belum ada post.")]))];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$3, {
									title: "User Terbaru",
									subtitle: "5 user terbaru",
									icon: unref(UserPlus),
									"action-label": "Lihat Semua",
									"action-to": "/dashboard/users"
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) if (recentUsers.value.length > 0) {
											_push(`<div class="space-y-3"${_scopeId}><!--[-->`);
											ssrRenderList(recentUsers.value, (u) => {
												_push(`<div class="flex items-center gap-3 p-3 rounded-lg border border-border/40 hover:bg-muted/30 hover:border-primary/20 transition-all"${_scopeId}><div class="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center text-primary text-sm font-bold shrink-0"${_scopeId}>${ssrInterpolate((u.fullname || u.username).charAt(0).toUpperCase())}</div><div class="flex-1 min-w-0"${_scopeId}><p class="text-sm font-medium truncate"${_scopeId}>${ssrInterpolate(u.fullname || u.username)}</p><p class="text-xs text-muted-foreground"${_scopeId}>@${ssrInterpolate(u.username)}</p></div></div>`);
											});
											_push(`<!--]--></div>`);
										} else {
											_push(`<div class="py-8 text-center"${_scopeId}>`);
											_push(ssrRenderComponent(unref(Users), { class: "w-8 h-8 mx-auto text-muted-foreground/40 mb-2" }, null, _parent, _scopeId));
											_push(`<p class="text-sm text-muted-foreground"${_scopeId}>Belum ada user.</p></div>`);
										}
										else return [recentUsers.value.length > 0 ? (openBlock(), createBlock("div", {
											key: 0,
											class: "space-y-3"
										}, [(openBlock(true), createBlock(Fragment, null, renderList(recentUsers.value, (u) => {
											return openBlock(), createBlock("div", {
												key: u.id,
												class: "flex items-center gap-3 p-3 rounded-lg border border-border/40 hover:bg-muted/30 hover:border-primary/20 transition-all"
											}, [createVNode("div", { class: "w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center text-primary text-sm font-bold shrink-0" }, toDisplayString((u.fullname || u.username).charAt(0).toUpperCase()), 1), createVNode("div", { class: "flex-1 min-w-0" }, [createVNode("p", { class: "text-sm font-medium truncate" }, toDisplayString(u.fullname || u.username), 1), createVNode("p", { class: "text-xs text-muted-foreground" }, "@" + toDisplayString(u.username), 1)])]);
										}), 128))])) : (openBlock(), createBlock("div", {
											key: 1,
											class: "py-8 text-center"
										}, [createVNode(unref(Users), { class: "w-8 h-8 mx-auto text-muted-foreground/40 mb-2" }), createVNode("p", { class: "text-sm text-muted-foreground" }, "Belum ada user.")]))];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$3, {
									title: "Aktivitas Terbaru",
									icon: unref(Activity)
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(ssrRenderComponent(_sfc_main$2, { items: recentActivities.value }, null, _parent, _scopeId));
										else return [createVNode(_sfc_main$2, { items: recentActivities.value }, null, 8, ["items"])];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`</div><div class="lg:col-span-2 space-y-4"${_scopeId}><h2 class="text-lg font-semibold tracking-tight"${_scopeId}>Statistik &amp; Data</h2>`);
								if (filteredStatCards.value.length > 0 && statsViewMode.value === "grid") {
									_push(`<div class="grid grid-cols-1 sm:grid-cols-2 gap-4"${_scopeId}><!--[-->`);
									ssrRenderList(filteredStatCards.value, (card) => {
										_push(ssrRenderComponent(_sfc_main$4, mergeProps({ key: card.label }, { ref_for: true }, card), null, _parent, _scopeId));
									});
									_push(`<!--]--></div>`);
								} else if (filteredStatCards.value.length > 0 && statsViewMode.value === "list") {
									_push(`<div class="flex flex-col gap-2"${_scopeId}><!--[-->`);
									ssrRenderList(filteredStatCards.value, (card) => {
										_push(ssrRenderComponent(_sfc_main$4, mergeProps({ key: card.label }, { ref_for: true }, card, { horizontal: true }), null, _parent, _scopeId));
									});
									_push(`<!--]--></div>`);
								} else {
									_push(`<div class="flex flex-col items-center justify-center py-20 text-center border rounded-xl border-dashed"${_scopeId}><div class="w-16 h-16 rounded-full bg-muted flex items-center justify-center mb-4"${_scopeId}>`);
									_push(ssrRenderComponent(unref(Activity), { class: "w-8 h-8 text-muted-foreground/40" }, null, _parent, _scopeId));
									_push(`</div><h3 class="text-lg font-semibold mb-1"${_scopeId}>Tidak Ada Akses</h3><p class="text-sm text-muted-foreground max-w-sm"${_scopeId}> Anda belum memiliki akses ke modul apapun. Hubungi administrator untuk meminta hak akses. </p></div>`);
								}
								_push(`</div></div>`);
							} else {
								_push(`<div class="hidden lg:block space-y-4"${_scopeId}><div class="flex items-center justify-between"${_scopeId}><h2 class="text-lg font-semibold tracking-tight"${_scopeId}>Statistik &amp; Data</h2><p class="text-sm text-muted-foreground"${_scopeId}>${ssrInterpolate(filteredStatCards.value.length)} hasil untuk <span class="font-medium text-foreground"${_scopeId}>&quot;${ssrInterpolate(searchQuery.value)}&quot;</span></p></div>`);
								if (filteredStatCards.value.length > 0 && statsViewMode.value === "grid") {
									_push(`<div class="grid grid-cols-2 xl:grid-cols-3 gap-4"${_scopeId}><!--[-->`);
									ssrRenderList(filteredStatCards.value, (card) => {
										_push(ssrRenderComponent(_sfc_main$4, mergeProps({ key: card.label }, { ref_for: true }, card), null, _parent, _scopeId));
									});
									_push(`<!--]--></div>`);
								} else if (filteredStatCards.value.length > 0 && statsViewMode.value === "list") {
									_push(`<div class="flex flex-col gap-2"${_scopeId}><!--[-->`);
									ssrRenderList(filteredStatCards.value, (card) => {
										_push(ssrRenderComponent(_sfc_main$4, mergeProps({ key: card.label }, { ref_for: true }, card, { horizontal: true }), null, _parent, _scopeId));
									});
									_push(`<!--]--></div>`);
								} else {
									_push(`<div class="flex flex-col items-center justify-center py-20 text-center border rounded-xl border-dashed"${_scopeId}><div class="w-16 h-16 rounded-full bg-muted flex items-center justify-center mb-4"${_scopeId}>`);
									_push(ssrRenderComponent(unref(Search), { class: "w-8 h-8 text-muted-foreground/40" }, null, _parent, _scopeId));
									_push(`</div><h3 class="text-lg font-semibold mb-1"${_scopeId}>Tidak ditemukan</h3><p class="text-sm text-muted-foreground max-w-sm"${_scopeId}> Tidak ada statistik yang cocok dengan &quot;${ssrInterpolate(searchQuery.value)}&quot;. Coba kata kunci lain. </p></div>`);
								}
								_push(`</div>`);
							}
							_push(`<!--]-->`);
						}
						_push(`</div>`);
						_push(ssrRenderComponent(_sfc_main$6, {
							"is-open": isAboutModalOpen.value,
							onClose: ($event) => isAboutModalOpen.value = false
						}, null, _parent, _scopeId));
					} else return [createVNode("div", { class: "space-y-6" }, [
						createVNode("div", { class: "flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between" }, [createVNode("div", null, [createVNode("h1", { class: "text-2xl font-bold tracking-tight" }, "Selamat datang, " + toDisplayString(unref(user)?.fullname || unref(user)?.username) + "! 👋", 1), createVNode("div", { class: "flex items-center gap-2 mt-1" }, [
							createVNode("p", { class: "text-sm text-muted-foreground" }, " Berikut ringkasan data dan aktivitas aplikasi hari ini. "),
							createVNode("span", { class: "text-zinc-300 dark:text-zinc-700" }, "•"),
							createVNode("button", {
								onClick: ($event) => isAboutModalOpen.value = true,
								class: "text-xs font-semibold text-primary hover:underline transition-all"
							}, " Tentang Kami ", 8, ["onClick"])
						])]), createVNode("div", { class: "flex items-center gap-2" }, [createVNode("span", { class: "text-xs text-muted-foreground" }, [createVNode(unref(Clock), { class: "w-3 h-3 inline mr-1" }), createTextVNode(" Terakhir diperbarui: " + toDisplayString(lastRefresh.value ? formatTime(lastRefresh.value) : "-"), 1)]), createVNode("button", {
							onClick: fetchStats,
							disabled: loading.value,
							class: "inline-flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-xs font-medium bg-primary text-primary-foreground hover:bg-primary/90 transition-colors disabled:opacity-50"
						}, [loading.value ? (openBlock(), createBlock(unref(Loader2), {
							key: 0,
							class: "w-3.5 h-3.5 animate-spin"
						})) : (openBlock(), createBlock(unref(Eye), {
							key: 1,
							class: "w-3.5 h-3.5"
						})), createTextVNode(" Refresh ")], 8, ["disabled"])])]),
						createVNode("div", { class: "flex items-center gap-2" }, [
							createVNode("div", { class: "relative flex-1" }, [createVNode(unref(Search), { class: "absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground pointer-events-none" }), withDirectives(createVNode("input", {
								"onUpdate:modelValue": ($event) => searchQuery.value = $event,
								type: "text",
								placeholder: "Cari statistik & data...",
								class: "w-full rounded-lg border border-border bg-card pl-9 pr-4 py-2 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary transition-all"
							}, null, 8, ["onUpdate:modelValue"]), [[vModelText, searchQuery.value]])]),
							createVNode("div", { class: "flex items-center gap-1 rounded-lg border border-border bg-card p-1 shrink-0" }, [createVNode("button", {
								onClick: ($event) => statsViewMode.value = "grid",
								class: ["rounded-md p-1.5 transition-colors", statsViewMode.value === "grid" ? "bg-primary text-primary-foreground" : "text-muted-foreground hover:text-foreground"],
								title: "Tampilan kotak"
							}, [createVNode(unref(LayoutGrid), { class: "w-4 h-4" })], 10, ["onClick"]), createVNode("button", {
								onClick: ($event) => statsViewMode.value = "list",
								class: ["rounded-md p-1.5 transition-colors", statsViewMode.value === "list" ? "bg-primary text-primary-foreground" : "text-muted-foreground hover:text-foreground"],
								title: "Tampilan daftar"
							}, [createVNode(unref(List), { class: "w-4 h-4" })], 10, ["onClick"])]),
							createVNode("div", {
								class: "relative shrink-0",
								ref_key: "dropdownRef",
								ref: dropdownRef
							}, [createVNode("button", {
								onClick: withModifiers(($event) => dropdownOpen.value = !dropdownOpen.value, ["stop"]),
								class: "inline-flex items-center gap-2 rounded-lg px-3.5 py-2 text-sm font-medium bg-primary text-primary-foreground hover:bg-primary/90 transition-all shadow-sm hover:shadow"
							}, [
								createVNode(unref(Plus), { class: "w-4 h-4" }),
								createTextVNode(" Buat Baru "),
								createVNode(unref(ChevronDown), { class: ["w-4 h-4 transition-transform duration-200", dropdownOpen.value ? "rotate-180" : ""] }, null, 8, ["class"])
							], 8, ["onClick"]), dropdownOpen.value ? (openBlock(), createBlock("div", {
								key: 0,
								class: "absolute right-0 top-full mt-1.5 w-44 rounded-xl border border-border bg-card shadow-lg z-50 overflow-hidden py-1"
							}, [(openBlock(true), createBlock(Fragment, null, renderList(createMenuItems.value, (item) => {
								return openBlock(), createBlock(_component_RouterLink, {
									key: item.label,
									to: item.to,
									onClick: ($event) => dropdownOpen.value = false,
									class: "flex items-center gap-2.5 px-3.5 py-2.5 text-sm hover:bg-muted/60 transition-colors"
								}, {
									default: withCtx(() => [(openBlock(), createBlock(resolveDynamicComponent(item.icon), { class: "w-4 h-4 text-muted-foreground" })), createTextVNode(" " + toDisplayString(item.label), 1)]),
									_: 2
								}, 1032, ["to", "onClick"]);
							}), 128)), createMenuItems.value.length === 0 ? (openBlock(), createBlock("div", {
								key: 0,
								class: "px-4 py-3 text-xs text-muted-foreground"
							}, " Tidak ada akses buat. ")) : createCommentVNode("", true)])) : createCommentVNode("", true)], 512)
						]),
						loading.value ? (openBlock(), createBlock("div", {
							key: 0,
							class: "flex items-center justify-center py-20"
						}, [createVNode("div", { class: "flex flex-col items-center gap-3" }, [createVNode(unref(Loader2), { class: "w-8 h-8 animate-spin text-primary" }), createVNode("p", { class: "text-sm text-muted-foreground" }, "Memuat data dashboard...")])])) : error.value ? (openBlock(), createBlock("div", {
							key: 1,
							class: "rounded-xl border border-destructive/30 bg-destructive/5 px-5 py-4"
						}, [createVNode("div", { class: "flex items-start gap-3" }, [createVNode(unref(Activity), { class: "w-5 h-5 text-destructive shrink-0 mt-0.5" }), createVNode("div", null, [createVNode("p", { class: "text-sm font-medium text-destructive" }, toDisplayString(error.value), 1), createVNode("p", { class: "text-xs text-muted-foreground mt-1" }, "Silakan coba refresh halaman.")])])])) : (openBlock(), createBlock(Fragment, { key: 2 }, [
							!searchQuery.value ? (openBlock(), createBlock(MobileSummaryTabs_default, {
								key: 0,
								"recent-posts": recentPosts.value,
								"recent-users": recentUsers.value,
								"recent-activities": recentActivities.value,
								"format-date": formatDate
							}, null, 8, [
								"recent-posts",
								"recent-users",
								"recent-activities"
							])) : createCommentVNode("", true),
							filteredStatCards.value.length > 0 ? (openBlock(), createBlock("div", {
								key: 1,
								class: ["lg:hidden", statsViewMode.value === "grid" ? "grid grid-cols-2 gap-3" : "flex flex-col gap-2"]
							}, [(openBlock(true), createBlock(Fragment, null, renderList(filteredStatCards.value, (card) => {
								return openBlock(), createBlock(_sfc_main$4, mergeProps({ key: card.label }, { ref_for: true }, card, { horizontal: statsViewMode.value === "list" }), null, 16, ["horizontal"]);
							}), 128))], 2)) : searchQuery.value && filteredStatCards.value.length === 0 ? (openBlock(), createBlock("div", {
								key: 2,
								class: "lg:hidden py-6 text-center text-sm text-muted-foreground"
							}, " Tidak ada statistik yang cocok dengan \"" + toDisplayString(searchQuery.value) + "\" ", 1)) : createCommentVNode("", true),
							!searchQuery.value ? (openBlock(), createBlock("div", {
								key: 3,
								class: "hidden lg:grid grid-cols-1 lg:grid-cols-3 gap-6"
							}, [createVNode("div", { class: "space-y-6 lg:col-span-1" }, [
								createVNode(_sfc_main$3, {
									title: "Post Terbaru",
									subtitle: "5 post terakhir",
									icon: unref(FileText),
									"action-label": "Lihat Semua",
									"action-to": "/dashboard/posts"
								}, {
									default: withCtx(() => [recentPosts.value.length > 0 ? (openBlock(), createBlock("div", {
										key: 0,
										class: "space-y-3"
									}, [(openBlock(true), createBlock(Fragment, null, renderList(recentPosts.value, (post) => {
										return openBlock(), createBlock(_component_RouterLink, {
											key: post.id,
											to: `/dashboard/posts`,
											class: "flex items-center justify-between p-3 rounded-lg border border-border/40 hover:bg-muted/30 hover:border-primary/20 transition-all group"
										}, {
											default: withCtx(() => [createVNode("div", { class: "flex-1 min-w-0" }, [createVNode("p", { class: "text-sm font-medium truncate group-hover:text-primary transition-colors" }, toDisplayString(post.title), 1), createVNode("div", { class: "flex items-center gap-2 mt-1" }, [createVNode("span", { class: ["inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-semibold", post.status === "published" ? "bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400" : "bg-amber-50 text-amber-700 dark:bg-amber-950/40 dark:text-amber-400"] }, toDisplayString(post.status === "published" ? "Published" : "Draft"), 3), createVNode("span", { class: "text-xs text-muted-foreground" }, toDisplayString(formatDate(post.createdAt)), 1)])]), createVNode(unref(ArrowRight), { class: "w-4 h-4 text-muted-foreground group-hover:text-primary group-hover:translate-x-0.5 transition-all" })]),
											_: 2
										}, 1024);
									}), 128))])) : (openBlock(), createBlock("div", {
										key: 1,
										class: "py-8 text-center"
									}, [createVNode(unref(FileText), { class: "w-8 h-8 mx-auto text-muted-foreground/40 mb-2" }), createVNode("p", { class: "text-sm text-muted-foreground" }, "Belum ada post.")]))]),
									_: 1
								}, 8, ["icon"]),
								createVNode(_sfc_main$3, {
									title: "User Terbaru",
									subtitle: "5 user terbaru",
									icon: unref(UserPlus),
									"action-label": "Lihat Semua",
									"action-to": "/dashboard/users"
								}, {
									default: withCtx(() => [recentUsers.value.length > 0 ? (openBlock(), createBlock("div", {
										key: 0,
										class: "space-y-3"
									}, [(openBlock(true), createBlock(Fragment, null, renderList(recentUsers.value, (u) => {
										return openBlock(), createBlock("div", {
											key: u.id,
											class: "flex items-center gap-3 p-3 rounded-lg border border-border/40 hover:bg-muted/30 hover:border-primary/20 transition-all"
										}, [createVNode("div", { class: "w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center text-primary text-sm font-bold shrink-0" }, toDisplayString((u.fullname || u.username).charAt(0).toUpperCase()), 1), createVNode("div", { class: "flex-1 min-w-0" }, [createVNode("p", { class: "text-sm font-medium truncate" }, toDisplayString(u.fullname || u.username), 1), createVNode("p", { class: "text-xs text-muted-foreground" }, "@" + toDisplayString(u.username), 1)])]);
									}), 128))])) : (openBlock(), createBlock("div", {
										key: 1,
										class: "py-8 text-center"
									}, [createVNode(unref(Users), { class: "w-8 h-8 mx-auto text-muted-foreground/40 mb-2" }), createVNode("p", { class: "text-sm text-muted-foreground" }, "Belum ada user.")]))]),
									_: 1
								}, 8, ["icon"]),
								createVNode(_sfc_main$3, {
									title: "Aktivitas Terbaru",
									icon: unref(Activity)
								}, {
									default: withCtx(() => [createVNode(_sfc_main$2, { items: recentActivities.value }, null, 8, ["items"])]),
									_: 1
								}, 8, ["icon"])
							]), createVNode("div", { class: "lg:col-span-2 space-y-4" }, [createVNode("h2", { class: "text-lg font-semibold tracking-tight" }, "Statistik & Data"), filteredStatCards.value.length > 0 && statsViewMode.value === "grid" ? (openBlock(), createBlock("div", {
								key: 0,
								class: "grid grid-cols-1 sm:grid-cols-2 gap-4"
							}, [(openBlock(true), createBlock(Fragment, null, renderList(filteredStatCards.value, (card) => {
								return openBlock(), createBlock(_sfc_main$4, mergeProps({ key: card.label }, { ref_for: true }, card), null, 16);
							}), 128))])) : filteredStatCards.value.length > 0 && statsViewMode.value === "list" ? (openBlock(), createBlock("div", {
								key: 1,
								class: "flex flex-col gap-2"
							}, [(openBlock(true), createBlock(Fragment, null, renderList(filteredStatCards.value, (card) => {
								return openBlock(), createBlock(_sfc_main$4, mergeProps({ key: card.label }, { ref_for: true }, card, { horizontal: true }), null, 16);
							}), 128))])) : (openBlock(), createBlock("div", {
								key: 2,
								class: "flex flex-col items-center justify-center py-20 text-center border rounded-xl border-dashed"
							}, [
								createVNode("div", { class: "w-16 h-16 rounded-full bg-muted flex items-center justify-center mb-4" }, [createVNode(unref(Activity), { class: "w-8 h-8 text-muted-foreground/40" })]),
								createVNode("h3", { class: "text-lg font-semibold mb-1" }, "Tidak Ada Akses"),
								createVNode("p", { class: "text-sm text-muted-foreground max-w-sm" }, " Anda belum memiliki akses ke modul apapun. Hubungi administrator untuk meminta hak akses. ")
							]))])])) : (openBlock(), createBlock("div", {
								key: 4,
								class: "hidden lg:block space-y-4"
							}, [createVNode("div", { class: "flex items-center justify-between" }, [createVNode("h2", { class: "text-lg font-semibold tracking-tight" }, "Statistik & Data"), createVNode("p", { class: "text-sm text-muted-foreground" }, [createTextVNode(toDisplayString(filteredStatCards.value.length) + " hasil untuk ", 1), createVNode("span", { class: "font-medium text-foreground" }, "\"" + toDisplayString(searchQuery.value) + "\"", 1)])]), filteredStatCards.value.length > 0 && statsViewMode.value === "grid" ? (openBlock(), createBlock("div", {
								key: 0,
								class: "grid grid-cols-2 xl:grid-cols-3 gap-4"
							}, [(openBlock(true), createBlock(Fragment, null, renderList(filteredStatCards.value, (card) => {
								return openBlock(), createBlock(_sfc_main$4, mergeProps({ key: card.label }, { ref_for: true }, card), null, 16);
							}), 128))])) : filteredStatCards.value.length > 0 && statsViewMode.value === "list" ? (openBlock(), createBlock("div", {
								key: 1,
								class: "flex flex-col gap-2"
							}, [(openBlock(true), createBlock(Fragment, null, renderList(filteredStatCards.value, (card) => {
								return openBlock(), createBlock(_sfc_main$4, mergeProps({ key: card.label }, { ref_for: true }, card, { horizontal: true }), null, 16);
							}), 128))])) : (openBlock(), createBlock("div", {
								key: 2,
								class: "flex flex-col items-center justify-center py-20 text-center border rounded-xl border-dashed"
							}, [
								createVNode("div", { class: "w-16 h-16 rounded-full bg-muted flex items-center justify-center mb-4" }, [createVNode(unref(Search), { class: "w-8 h-8 text-muted-foreground/40" })]),
								createVNode("h3", { class: "text-lg font-semibold mb-1" }, "Tidak ditemukan"),
								createVNode("p", { class: "text-sm text-muted-foreground max-w-sm" }, " Tidak ada statistik yang cocok dengan \"" + toDisplayString(searchQuery.value) + "\". Coba kata kunci lain. ", 1)
							]))]))
						], 64))
					]), createVNode(_sfc_main$6, {
						"is-open": isAboutModalOpen.value,
						onClose: ($event) => isAboutModalOpen.value = false
					}, null, 8, ["is-open", "onClose"])];
				}),
				_: 1
			}, _parent));
		};
	}
};
var _sfc_setup = _sfc_main.setup;
_sfc_main.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/pages/DashboardPage.vue");
	return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
//#endregion
export { _sfc_main as default };
