import { n as useAuthStore, t as useThemeStore } from "../main.mjs";
import { n as cn, t as _sfc_main$19 } from "./Button-Bj0EF1Kv.js";
import { Fragment, computed, createBlock, createCommentVNode, createTextVNode, createVNode, mergeDefaults, mergeProps, onBeforeUnmount, onMounted, openBlock, ref, renderList, renderSlot, resolveComponent, resolveDynamicComponent, toDisplayString, unref, useSSRContext, watch, withCtx } from "vue";
import { useRoute, useRouter } from "vue-router";
import { defineStore, storeToRefs } from "pinia";
import { ssrInterpolate, ssrRenderAttr, ssrRenderAttrs, ssrRenderClass, ssrRenderComponent, ssrRenderList, ssrRenderSlot, ssrRenderStyle, ssrRenderTeleport, ssrRenderVNode } from "vue/server-renderer";
import { Activity, AlertCircle, ArrowLeftRight, Check, CheckCircle2, ChevronDown, ChevronRight, Circle, FileText, Globe, Heart, HelpCircle, Home, Info, KeyRound, LayoutDashboard, Loader2, LogOut, MapPin, Monitor, Moon, PanelLeftOpen, Rocket, ScrollText, Search, Settings, Shield, ShieldCheck, ShoppingCart, Sun, Ticket, Users, X, Zap } from "lucide-vue-next";
import { cva } from "class-variance-authority";
import { DropdownMenuCheckboxItem, DropdownMenuContent, DropdownMenuGroup, DropdownMenuItem, DropdownMenuItemIndicator, DropdownMenuLabel, DropdownMenuPortal, DropdownMenuPortal as DropdownMenuPortal$1, DropdownMenuRadioGroup, DropdownMenuRadioItem, DropdownMenuRoot, DropdownMenuSeparator, DropdownMenuSub, DropdownMenuSubContent, DropdownMenuSubTrigger, DropdownMenuTrigger, useForwardProps, useForwardPropsEmits } from "reka-ui";
import { reactiveOmit } from "@vueuse/core";
//#region src/composables/usePermission.js
/**
* Composable for checking user permissions.
*
* Usage:
*   const { can, canAny, canAll } = usePermission()
*   if (can('post.store')) { ... }
*   v-if="can('post.update')"
*/
function usePermission() {
	const { permissions } = storeToRefs(useAuthStore());
	/**
	* Check if the user has a single permission slug.
	* @param {string} slug  e.g. 'post.index'
	*/
	function can(slug) {
		return permissions.value.includes(slug);
	}
	/**
	* Check if the user has ANY of the given permission slugs.
	* @param {...string} slugs
	*/
	function canAny(...slugs) {
		return slugs.some((slug) => permissions.value.includes(slug));
	}
	/**
	* Check if the user has ALL of the given permission slugs.
	* @param {...string} slugs
	*/
	function canAll(...slugs) {
		return slugs.every((slug) => permissions.value.includes(slug));
	}
	return {
		can,
		canAny,
		canAll
	};
}
//#endregion
//#region src/stores/toast.js
var nextId = 1;
var useToastStore = defineStore("toast", () => {
	const toasts = ref([]);
	function add(message, type = "default", duration = 3e3) {
		const id = nextId++;
		toasts.value.push({
			id,
			message,
			type
		});
		if (duration > 0) setTimeout(() => remove(id), duration);
		return id;
	}
	function remove(id) {
		const idx = toasts.value.findIndex((t) => t.id === id);
		if (idx !== -1) toasts.value.splice(idx, 1);
	}
	function success(message, duration = 3e3) {
		return add(message, "success", duration);
	}
	function error(message, duration = 5e3) {
		return add(message, "error", duration);
	}
	function info(message, duration = 3e3) {
		return add(message, "info", duration);
	}
	return {
		toasts,
		add,
		remove,
		success,
		error,
		info
	};
});
//#endregion
//#region src/stores/confirm.js
var useConfirmStore = defineStore("confirm", () => {
	const isOpen = ref(false);
	const config = ref({
		title: "",
		description: "",
		confirmLabel: "Confirm",
		cancelLabel: "Cancel",
		variant: "destructive"
	});
	const loading = ref(false);
	let resolver = null;
	function confirm(opts = {}) {
		return new Promise((resolve) => {
			resolver = resolve;
			config.value = {
				title: opts.title || "Are you sure?",
				description: opts.description || "This action cannot be undone.",
				confirmLabel: opts.confirmLabel || "Confirm",
				cancelLabel: opts.cancelLabel || "Cancel",
				variant: opts.variant || "destructive"
			};
			loading.value = false;
			isOpen.value = true;
		});
	}
	function onConfirm() {
		if (resolver) resolver(true);
		resolver = null;
		isOpen.value = false;
		loading.value = false;
	}
	function onCancel() {
		if (resolver) resolver(false);
		resolver = null;
		isOpen.value = false;
		loading.value = false;
	}
	return {
		isOpen,
		config,
		loading,
		confirm,
		onConfirm,
		onCancel
	};
});
//#endregion
//#region src/components/ui/Toast.vue
var _sfc_main$18 = {
	__name: "Toast",
	__ssrInlineRender: true,
	setup(__props) {
		const store = useToastStore();
		const icons = {
			success: CheckCircle2,
			error: AlertCircle,
			info: Info
		};
		const variants = {
			success: "border-green-300 text-green-800 bg-green-50",
			error: "border-destructive/50 text-destructive bg-destructive/10",
			info: "border-blue-300 text-blue-800 bg-blue-50"
		};
		return (_ctx, _push, _parent, _attrs) => {
			_push(`<div${ssrRenderAttrs(mergeProps({ class: "fixed top-4 right-4 z-[100] flex flex-col gap-2 w-80 pointer-events-none" }, _attrs))}><!--[-->`);
			ssrRenderList(unref(store).toasts, (t) => {
				_push(`<div class="${ssrRenderClass(unref(cn)("pointer-events-auto rounded-lg border p-3 text-sm shadow-lg flex items-start gap-2", variants[t.type] || variants.info))}">`);
				ssrRenderVNode(_push, createVNode(resolveDynamicComponent(icons[t.type] || icons.info), { class: "h-4 w-4 mt-0.5 shrink-0" }, null), _parent);
				_push(`<span class="flex-1">${ssrInterpolate(t.message)}</span><button class="shrink-0 opacity-60 hover:opacity-100 transition-opacity">`);
				_push(ssrRenderComponent(unref(X), { class: "h-3.5 w-3.5" }, null, _parent));
				_push(`</button></div>`);
			});
			_push(`<!--]--></div>`);
		};
	}
};
var _sfc_setup$18 = _sfc_main$18.setup;
_sfc_main$18.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/Toast.vue");
	return _sfc_setup$18 ? _sfc_setup$18(props, ctx) : void 0;
};
//#endregion
//#region src/components/ui/ConfirmDialog.vue
var _sfc_main$17 = {
	__name: "ConfirmDialog",
	__ssrInlineRender: true,
	props: /* @__PURE__ */ mergeDefaults({
		title: String,
		description: String,
		confirmLabel: String,
		cancelLabel: String,
		variant: String,
		loading: Boolean
	}, {
		title: "Are you sure?",
		description: "This action cannot be undone.",
		confirmLabel: "Confirm",
		cancelLabel: "Cancel",
		variant: "destructive",
		loading: false
	}),
	emits: ["confirm", "cancel"],
	setup(__props, { emit: __emit }) {
		const emit = __emit;
		return (_ctx, _push, _parent, _attrs) => {
			ssrRenderTeleport(_push, (_push) => {
				_push(`<div class="fixed inset-0 z-[90] flex items-center justify-center"><div class="absolute inset-0 bg-black/50"></div><div class="relative z-10 w-full max-w-sm mx-4 bg-card rounded-lg shadow-xl border p-6 space-y-4"><h3 class="font-semibold text-lg">${ssrInterpolate(__props.title)}</h3><p class="text-sm text-muted-foreground">${ssrInterpolate(__props.description)}</p><div class="flex justify-end gap-3">`);
				_push(ssrRenderComponent(_sfc_main$19, {
					variant: "outline",
					onClick: ($event) => emit("cancel"),
					disabled: __props.loading
				}, {
					default: withCtx((_, _push, _parent, _scopeId) => {
						if (_push) _push(`${ssrInterpolate(__props.cancelLabel)}`);
						else return [createTextVNode(toDisplayString(__props.cancelLabel), 1)];
					}),
					_: 1
				}, _parent));
				_push(ssrRenderComponent(_sfc_main$19, {
					variant: __props.variant,
					onClick: ($event) => emit("confirm"),
					disabled: __props.loading
				}, {
					default: withCtx((_, _push, _parent, _scopeId) => {
						if (_push) {
							if (__props.loading) _push(ssrRenderComponent(unref(Loader2), { class: "h-4 w-4 mr-2 animate-spin" }, null, _parent, _scopeId));
							else _push(`<!---->`);
							_push(` ${ssrInterpolate(__props.confirmLabel)}`);
						} else return [__props.loading ? (openBlock(), createBlock(unref(Loader2), {
							key: 0,
							class: "h-4 w-4 mr-2 animate-spin"
						})) : createCommentVNode("", true), createTextVNode(" " + toDisplayString(__props.confirmLabel), 1)];
					}),
					_: 1
				}, _parent));
				_push(`</div></div></div>`);
			}, "body", false, _parent);
		};
	}
};
var _sfc_setup$17 = _sfc_main$17.setup;
_sfc_main$17.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/ConfirmDialog.vue");
	return _sfc_setup$17 ? _sfc_setup$17(props, ctx) : void 0;
};
//#endregion
//#region src/components/dashboard/AboutModal.vue
var _sfc_main$16 = {
	__name: "AboutModal",
	__ssrInlineRender: true,
	props: { isOpen: Boolean },
	emits: ["close"],
	setup(__props, { emit: __emit }) {
		return (_ctx, _push, _parent, _attrs) => {
			if (__props.isOpen) {
				_push(`<div${ssrRenderAttrs(mergeProps({ class: "fixed inset-0 z-[100] flex items-center justify-center p-4 sm:p-6" }, _attrs))}><div class="absolute inset-0 bg-zinc-950/60 backdrop-blur-md"></div><div class="relative w-full max-w-2xl overflow-hidden rounded-3xl bg-white dark:bg-zinc-900 shadow-2xl border border-white/20 dark:border-zinc-800"><div class="absolute -top-24 -right-24 w-64 h-64 bg-primary/20 rounded-full blur-3xl"></div><div class="absolute -bottom-24 -left-24 w-64 h-64 bg-blue-500/10 rounded-full blur-3xl"></div><div class="relative flex items-center justify-between p-6 border-b border-border/50"><div class="flex items-center gap-3"><div class="w-10 h-10 bg-primary rounded-xl flex items-center justify-center shadow-lg shadow-primary/20">`);
				_push(ssrRenderComponent(unref(Zap), { class: "w-6 h-6 text-primary-foreground" }, null, _parent));
				_push(`</div><div><h2 class="text-xl font-bold tracking-tight">Tentang Gaptek</h2><p class="text-xs text-muted-foreground">Versi 1.0.0 • Premium Dashboard</p></div></div><button class="p-2 rounded-full hover:bg-muted transition-colors text-muted-foreground hover:text-foreground">`);
				_push(ssrRenderComponent(unref(X), { class: "w-5 h-5" }, null, _parent));
				_push(`</button></div><div class="relative p-8 space-y-8 overflow-y-auto max-h-[70vh]"><div class="space-y-4"><h3 class="text-3xl font-bold leading-tight bg-gradient-to-r from-primary to-blue-600 bg-clip-text text-transparent"> Masa Depan Manajemen Bisnis Ada di Tangan Anda. </h3><p class="text-zinc-600 dark:text-zinc-400 leading-relaxed text-lg"> Gaptek bukan sekadar aplikasi manajemen. Kami adalah partner strategis yang membantu Anda mengubah kompleksitas operasional menjadi kemudahan yang elegan. Dirancang untuk kecepatan, dibangun untuk pertumbuhan. </p></div><div class="grid grid-cols-1 md:grid-cols-2 gap-4"><div class="p-4 rounded-2xl bg-zinc-50 dark:bg-zinc-800/50 border border-border/50 flex gap-4"><div class="w-12 h-12 rounded-xl bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center shrink-0">`);
				_push(ssrRenderComponent(unref(Rocket), { class: "w-6 h-6 text-blue-600 dark:text-blue-400" }, null, _parent));
				_push(`</div><div><h4 class="font-bold text-sm">Performa Maksimal</h4><p class="text-xs text-muted-foreground mt-1">Kecepatan akses data real-time tanpa kompromi.</p></div></div><div class="p-4 rounded-2xl bg-zinc-50 dark:bg-zinc-800/50 border border-border/50 flex gap-4"><div class="w-12 h-12 rounded-xl bg-rose-100 dark:bg-rose-900/30 flex items-center justify-center shrink-0">`);
				_push(ssrRenderComponent(unref(Heart), { class: "w-6 h-6 text-rose-600 dark:text-rose-400" }, null, _parent));
				_push(`</div><div><h4 class="font-bold text-sm">User Centric</h4><p class="text-xs text-muted-foreground mt-1">Antarmuka intuitif yang memanjakan mata.</p></div></div><div class="p-4 rounded-2xl bg-zinc-50 dark:bg-zinc-800/50 border border-border/50 flex gap-4"><div class="w-12 h-12 rounded-xl bg-emerald-100 dark:bg-emerald-900/30 flex items-center justify-center shrink-0">`);
				_push(ssrRenderComponent(unref(Shield), { class: "w-6 h-6 text-emerald-600 dark:text-emerald-400" }, null, _parent));
				_push(`</div><div><h4 class="font-bold text-sm">Keamanan Ketat</h4><p class="text-xs text-muted-foreground mt-1">Data Anda terlindungi dengan enkripsi standar industri.</p></div></div><div class="p-4 rounded-2xl bg-zinc-50 dark:bg-zinc-800/50 border border-border/50 flex gap-4"><div class="w-12 h-12 rounded-xl bg-amber-100 dark:bg-amber-900/30 flex items-center justify-center shrink-0">`);
				_push(ssrRenderComponent(unref(Globe), { class: "w-6 h-6 text-amber-600 dark:text-amber-400" }, null, _parent));
				_push(`</div><div><h4 class="font-bold text-sm">Akses Dimanapun</h4><p class="text-xs text-muted-foreground mt-1">Pantau bisnis Anda kapan saja, di mana saja.</p></div></div></div><div class="pt-4 text-center"><p class="text-sm font-medium italic text-muted-foreground"> &quot;Building the bridge between technology and your business success.&quot; </p><div class="mt-6 flex justify-center gap-4"><div class="px-4 py-2 rounded-full bg-zinc-100 dark:bg-zinc-800 text-[10px] font-bold uppercase tracking-wider text-zinc-500"> #ModernERP </div><div class="px-4 py-2 rounded-full bg-zinc-100 dark:bg-zinc-800 text-[10px] font-bold uppercase tracking-wider text-zinc-500"> #Efficiency </div><div class="px-4 py-2 rounded-full bg-zinc-100 dark:bg-zinc-800 text-[10px] font-bold uppercase tracking-wider text-zinc-500"> #Growth </div></div></div></div><div class="p-6 bg-zinc-50 dark:bg-zinc-800/30 border-t border-border/50 flex justify-end"><button class="px-6 py-2.5 rounded-xl bg-zinc-900 dark:bg-white text-white dark:text-zinc-900 font-bold text-sm hover:scale-105 transition-transform active:scale-95 shadow-lg shadow-black/10"> Tutup &amp; Lanjutkan </button></div></div></div>`);
			} else _push(`<!---->`);
		};
	}
};
var _sfc_setup$16 = _sfc_main$16.setup;
_sfc_main$16.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/dashboard/AboutModal.vue");
	return _sfc_setup$16 ? _sfc_setup$16(props, ctx) : void 0;
};
//#endregion
//#region src/components/ui/button/Button.vue
var _sfc_main$15 = {
	__name: "Button",
	__ssrInlineRender: true,
	props: {
		variant: {
			type: String,
			default: "default"
		},
		size: {
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
			_push(`<button${ssrRenderAttrs(mergeProps(delegatedProps.value, { class: unref(cn)(unref(buttonVariants)({
				variant: __props.variant,
				size: __props.size
			}), props.class) }, _attrs))}>`);
			ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent);
			_push(`</button>`);
		};
	}
};
var _sfc_setup$15 = _sfc_main$15.setup;
_sfc_main$15.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/button/Button.vue");
	return _sfc_setup$15 ? _sfc_setup$15(props, ctx) : void 0;
};
//#endregion
//#region src/components/ui/button/index.js
var buttonVariants = cva("inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium ring-offset-background transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 active:scale-[0.98]", {
	variants: {
		variant: {
			default: "bg-primary text-primary-foreground hover:bg-primary/90",
			destructive: "bg-destructive text-destructive-foreground hover:bg-destructive/90",
			outline: "border border-input bg-background hover:bg-zinc-100 dark:hover:bg-zinc-900",
			secondary: "bg-secondary text-secondary-foreground hover:bg-secondary/80",
			ghost: "hover:bg-accent hover:text-accent-foreground",
			link: "text-primary underline-offset-4 hover:underline"
		},
		size: {
			default: "h-9 px-4 py-2",
			sm: "h-8 px-3 text-[11px]",
			lg: "h-10 rounded-md px-8",
			icon: "h-9 w-9"
		}
	},
	defaultVariants: {
		variant: "default",
		size: "default"
	}
});
//#endregion
//#region src/components/ui/dropdown-menu/DropdownMenu.vue
var _sfc_main$14 = {
	__name: "DropdownMenu",
	__ssrInlineRender: true,
	props: {
		defaultOpen: {
			type: Boolean,
			required: false
		},
		open: {
			type: Boolean,
			required: false
		},
		dir: {
			type: String,
			required: false
		},
		modal: {
			type: Boolean,
			required: false
		}
	},
	emits: ["update:open"],
	setup(__props, { emit: __emit }) {
		const forwarded = useForwardPropsEmits(__props, __emit);
		return (_ctx, _push, _parent, _attrs) => {
			_push(ssrRenderComponent(unref(DropdownMenuRoot), mergeProps(unref(forwarded), _attrs), {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent, _scopeId);
					else return [renderSlot(_ctx.$slots, "default")];
				}),
				_: 3
			}, _parent));
		};
	}
};
var _sfc_setup$14 = _sfc_main$14.setup;
_sfc_main$14.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/dropdown-menu/DropdownMenu.vue");
	return _sfc_setup$14 ? _sfc_setup$14(props, ctx) : void 0;
};
//#endregion
//#region src/components/ui/dropdown-menu/DropdownMenuCheckboxItem.vue
var _sfc_main$13 = {
	__name: "DropdownMenuCheckboxItem",
	__ssrInlineRender: true,
	props: {
		modelValue: {
			type: [Boolean, String],
			required: false
		},
		disabled: {
			type: Boolean,
			required: false
		},
		textValue: {
			type: String,
			required: false
		},
		asChild: {
			type: Boolean,
			required: false
		},
		as: {
			type: null,
			required: false
		},
		class: {
			type: null,
			required: false
		}
	},
	emits: ["select", "update:modelValue"],
	setup(__props, { emit: __emit }) {
		const props = __props;
		const emits = __emit;
		const forwarded = useForwardPropsEmits(reactiveOmit(props, "class"), emits);
		return (_ctx, _push, _parent, _attrs) => {
			_push(ssrRenderComponent(unref(DropdownMenuCheckboxItem), mergeProps(unref(forwarded), { class: unref(cn)("relative flex cursor-default select-none items-center rounded-sm py-1.5 pl-8 pr-2 text-sm outline-none transition-colors focus:bg-accent focus:text-accent-foreground data-[disabled]:pointer-events-none data-[disabled]:opacity-50", props.class) }, _attrs), {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) {
						_push(`<span class="absolute left-2 flex h-3.5 w-3.5 items-center justify-center"${_scopeId}>`);
						_push(ssrRenderComponent(unref(DropdownMenuItemIndicator), null, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) _push(ssrRenderComponent(unref(Check), { class: "w-4 h-4" }, null, _parent, _scopeId));
								else return [createVNode(unref(Check), { class: "w-4 h-4" })];
							}),
							_: 1
						}, _parent, _scopeId));
						_push(`</span>`);
						ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent, _scopeId);
					} else return [createVNode("span", { class: "absolute left-2 flex h-3.5 w-3.5 items-center justify-center" }, [createVNode(unref(DropdownMenuItemIndicator), null, {
						default: withCtx(() => [createVNode(unref(Check), { class: "w-4 h-4" })]),
						_: 1
					})]), renderSlot(_ctx.$slots, "default")];
				}),
				_: 3
			}, _parent));
		};
	}
};
var _sfc_setup$13 = _sfc_main$13.setup;
_sfc_main$13.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/dropdown-menu/DropdownMenuCheckboxItem.vue");
	return _sfc_setup$13 ? _sfc_setup$13(props, ctx) : void 0;
};
//#endregion
//#region src/components/ui/dropdown-menu/DropdownMenuContent.vue
var _sfc_main$12 = {
	__name: "DropdownMenuContent",
	__ssrInlineRender: true,
	props: {
		forceMount: {
			type: Boolean,
			required: false
		},
		loop: {
			type: Boolean,
			required: false
		},
		side: {
			type: null,
			required: false
		},
		sideOffset: {
			type: Number,
			required: false,
			default: 4
		},
		sideFlip: {
			type: Boolean,
			required: false
		},
		align: {
			type: null,
			required: false
		},
		alignOffset: {
			type: Number,
			required: false
		},
		alignFlip: {
			type: Boolean,
			required: false
		},
		avoidCollisions: {
			type: Boolean,
			required: false
		},
		collisionBoundary: {
			type: null,
			required: false
		},
		collisionPadding: {
			type: [Number, Object],
			required: false
		},
		arrowPadding: {
			type: Number,
			required: false
		},
		hideShiftedArrow: {
			type: Boolean,
			required: false
		},
		sticky: {
			type: String,
			required: false
		},
		hideWhenDetached: {
			type: Boolean,
			required: false
		},
		positionStrategy: {
			type: String,
			required: false
		},
		updatePositionStrategy: {
			type: String,
			required: false
		},
		disableUpdateOnLayoutShift: {
			type: Boolean,
			required: false
		},
		prioritizePosition: {
			type: Boolean,
			required: false
		},
		reference: {
			type: null,
			required: false
		},
		asChild: {
			type: Boolean,
			required: false
		},
		as: {
			type: null,
			required: false
		},
		class: {
			type: null,
			required: false
		}
	},
	emits: [
		"escapeKeyDown",
		"pointerDownOutside",
		"focusOutside",
		"interactOutside",
		"closeAutoFocus"
	],
	setup(__props, { emit: __emit }) {
		const props = __props;
		const emits = __emit;
		const forwarded = useForwardPropsEmits(reactiveOmit(props, "class"), emits);
		return (_ctx, _push, _parent, _attrs) => {
			_push(ssrRenderComponent(unref(DropdownMenuPortal), _attrs, {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) _push(ssrRenderComponent(unref(DropdownMenuContent), mergeProps(unref(forwarded), { class: unref(cn)("z-50 min-w-32 overflow-hidden rounded-md border bg-white dark:bg-zinc-950 p-1 text-zinc-900 dark:text-zinc-100 shadow-md data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0 data-[state=closed]:zoom-out-95 data-[state=open]:zoom-in-95 data-[side=bottom]:slide-in-from-top-2 data-[side=left]:slide-in-from-right-2 data-[side=right]:slide-in-from-left-2 data-[side=top]:slide-in-from-bottom-2", props.class) }), {
						default: withCtx((_, _push, _parent, _scopeId) => {
							if (_push) ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent, _scopeId);
							else return [renderSlot(_ctx.$slots, "default")];
						}),
						_: 3
					}, _parent, _scopeId));
					else return [createVNode(unref(DropdownMenuContent), mergeProps(unref(forwarded), { class: unref(cn)("z-50 min-w-32 overflow-hidden rounded-md border bg-white dark:bg-zinc-950 p-1 text-zinc-900 dark:text-zinc-100 shadow-md data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0 data-[state=closed]:zoom-out-95 data-[state=open]:zoom-in-95 data-[side=bottom]:slide-in-from-top-2 data-[side=left]:slide-in-from-right-2 data-[side=right]:slide-in-from-left-2 data-[side=top]:slide-in-from-bottom-2", props.class) }), {
						default: withCtx(() => [renderSlot(_ctx.$slots, "default")]),
						_: 3
					}, 16, ["class"])];
				}),
				_: 3
			}, _parent));
		};
	}
};
var _sfc_setup$12 = _sfc_main$12.setup;
_sfc_main$12.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/dropdown-menu/DropdownMenuContent.vue");
	return _sfc_setup$12 ? _sfc_setup$12(props, ctx) : void 0;
};
//#endregion
//#region src/components/ui/dropdown-menu/DropdownMenuGroup.vue
var _sfc_main$11 = {
	__name: "DropdownMenuGroup",
	__ssrInlineRender: true,
	props: {
		asChild: {
			type: Boolean,
			required: false
		},
		as: {
			type: null,
			required: false
		}
	},
	setup(__props) {
		const props = __props;
		return (_ctx, _push, _parent, _attrs) => {
			_push(ssrRenderComponent(unref(DropdownMenuGroup), mergeProps(props, _attrs), {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent, _scopeId);
					else return [renderSlot(_ctx.$slots, "default")];
				}),
				_: 3
			}, _parent));
		};
	}
};
var _sfc_setup$11 = _sfc_main$11.setup;
_sfc_main$11.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/dropdown-menu/DropdownMenuGroup.vue");
	return _sfc_setup$11 ? _sfc_setup$11(props, ctx) : void 0;
};
//#endregion
//#region src/components/ui/dropdown-menu/DropdownMenuItem.vue
var _sfc_main$10 = {
	__name: "DropdownMenuItem",
	__ssrInlineRender: true,
	props: {
		disabled: {
			type: Boolean,
			required: false
		},
		textValue: {
			type: String,
			required: false
		},
		asChild: {
			type: Boolean,
			required: false
		},
		as: {
			type: null,
			required: false
		},
		class: {
			type: null,
			required: false
		},
		inset: {
			type: Boolean,
			required: false
		}
	},
	setup(__props) {
		const props = __props;
		const forwardedProps = useForwardProps(reactiveOmit(props, "class"));
		return (_ctx, _push, _parent, _attrs) => {
			_push(ssrRenderComponent(unref(DropdownMenuItem), mergeProps(unref(forwardedProps), { class: unref(cn)("relative flex cursor-default select-none items-center rounded-sm gap-2 px-2 py-1.5 text-sm outline-none transition-colors focus:bg-accent focus:text-accent-foreground data-[disabled]:pointer-events-none data-[disabled]:opacity-50 [&>svg]:size-4 [&>svg]:shrink-0", __props.inset && "pl-8", props.class) }, _attrs), {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent, _scopeId);
					else return [renderSlot(_ctx.$slots, "default")];
				}),
				_: 3
			}, _parent));
		};
	}
};
var _sfc_setup$10 = _sfc_main$10.setup;
_sfc_main$10.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/dropdown-menu/DropdownMenuItem.vue");
	return _sfc_setup$10 ? _sfc_setup$10(props, ctx) : void 0;
};
//#endregion
//#region src/components/ui/dropdown-menu/DropdownMenuLabel.vue
var _sfc_main$9 = {
	__name: "DropdownMenuLabel",
	__ssrInlineRender: true,
	props: {
		asChild: {
			type: Boolean,
			required: false
		},
		as: {
			type: null,
			required: false
		},
		class: {
			type: null,
			required: false
		},
		inset: {
			type: Boolean,
			required: false
		}
	},
	setup(__props) {
		const props = __props;
		const forwardedProps = useForwardProps(reactiveOmit(props, "class"));
		return (_ctx, _push, _parent, _attrs) => {
			_push(ssrRenderComponent(unref(DropdownMenuLabel), mergeProps(unref(forwardedProps), { class: unref(cn)("px-2 py-1.5 text-sm font-semibold", __props.inset && "pl-8", props.class) }, _attrs), {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent, _scopeId);
					else return [renderSlot(_ctx.$slots, "default")];
				}),
				_: 3
			}, _parent));
		};
	}
};
var _sfc_setup$9 = _sfc_main$9.setup;
_sfc_main$9.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/dropdown-menu/DropdownMenuLabel.vue");
	return _sfc_setup$9 ? _sfc_setup$9(props, ctx) : void 0;
};
//#endregion
//#region src/components/ui/dropdown-menu/DropdownMenuRadioGroup.vue
var _sfc_main$8 = {
	__name: "DropdownMenuRadioGroup",
	__ssrInlineRender: true,
	props: {
		modelValue: {
			type: null,
			required: false
		},
		asChild: {
			type: Boolean,
			required: false
		},
		as: {
			type: null,
			required: false
		}
	},
	emits: ["update:modelValue"],
	setup(__props, { emit: __emit }) {
		const forwarded = useForwardPropsEmits(__props, __emit);
		return (_ctx, _push, _parent, _attrs) => {
			_push(ssrRenderComponent(unref(DropdownMenuRadioGroup), mergeProps(unref(forwarded), _attrs), {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent, _scopeId);
					else return [renderSlot(_ctx.$slots, "default")];
				}),
				_: 3
			}, _parent));
		};
	}
};
var _sfc_setup$8 = _sfc_main$8.setup;
_sfc_main$8.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/dropdown-menu/DropdownMenuRadioGroup.vue");
	return _sfc_setup$8 ? _sfc_setup$8(props, ctx) : void 0;
};
//#endregion
//#region src/components/ui/dropdown-menu/DropdownMenuRadioItem.vue
var _sfc_main$7 = {
	__name: "DropdownMenuRadioItem",
	__ssrInlineRender: true,
	props: {
		value: {
			type: null,
			required: true
		},
		disabled: {
			type: Boolean,
			required: false
		},
		textValue: {
			type: String,
			required: false
		},
		asChild: {
			type: Boolean,
			required: false
		},
		as: {
			type: null,
			required: false
		},
		class: {
			type: null,
			required: false
		}
	},
	emits: ["select"],
	setup(__props, { emit: __emit }) {
		const props = __props;
		const emits = __emit;
		const forwarded = useForwardPropsEmits(reactiveOmit(props, "class"), emits);
		return (_ctx, _push, _parent, _attrs) => {
			_push(ssrRenderComponent(unref(DropdownMenuRadioItem), mergeProps(unref(forwarded), { class: unref(cn)("relative flex cursor-default select-none items-center rounded-sm py-1.5 pl-8 pr-2 text-sm outline-none transition-colors focus:bg-accent focus:text-accent-foreground data-[disabled]:pointer-events-none data-[disabled]:opacity-50", props.class) }, _attrs), {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) {
						_push(`<span class="absolute left-2 flex h-3.5 w-3.5 items-center justify-center"${_scopeId}>`);
						_push(ssrRenderComponent(unref(DropdownMenuItemIndicator), null, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) _push(ssrRenderComponent(unref(Circle), { class: "h-2 w-2 fill-current" }, null, _parent, _scopeId));
								else return [createVNode(unref(Circle), { class: "h-2 w-2 fill-current" })];
							}),
							_: 1
						}, _parent, _scopeId));
						_push(`</span>`);
						ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent, _scopeId);
					} else return [createVNode("span", { class: "absolute left-2 flex h-3.5 w-3.5 items-center justify-center" }, [createVNode(unref(DropdownMenuItemIndicator), null, {
						default: withCtx(() => [createVNode(unref(Circle), { class: "h-2 w-2 fill-current" })]),
						_: 1
					})]), renderSlot(_ctx.$slots, "default")];
				}),
				_: 3
			}, _parent));
		};
	}
};
var _sfc_setup$7 = _sfc_main$7.setup;
_sfc_main$7.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/dropdown-menu/DropdownMenuRadioItem.vue");
	return _sfc_setup$7 ? _sfc_setup$7(props, ctx) : void 0;
};
//#endregion
//#region src/components/ui/dropdown-menu/DropdownMenuSeparator.vue
var _sfc_main$6 = {
	__name: "DropdownMenuSeparator",
	__ssrInlineRender: true,
	props: {
		asChild: {
			type: Boolean,
			required: false
		},
		as: {
			type: null,
			required: false
		},
		class: {
			type: null,
			required: false
		}
	},
	setup(__props) {
		const props = __props;
		const delegatedProps = reactiveOmit(props, "class");
		return (_ctx, _push, _parent, _attrs) => {
			_push(ssrRenderComponent(unref(DropdownMenuSeparator), mergeProps(unref(delegatedProps), { class: unref(cn)("-mx-1 my-1 h-px bg-muted", props.class) }, _attrs), null, _parent));
		};
	}
};
var _sfc_setup$6 = _sfc_main$6.setup;
_sfc_main$6.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/dropdown-menu/DropdownMenuSeparator.vue");
	return _sfc_setup$6 ? _sfc_setup$6(props, ctx) : void 0;
};
//#endregion
//#region src/components/ui/dropdown-menu/DropdownMenuShortcut.vue
var _sfc_main$5 = {
	__name: "DropdownMenuShortcut",
	__ssrInlineRender: true,
	props: { class: {
		type: null,
		required: false
	} },
	setup(__props) {
		const props = __props;
		return (_ctx, _push, _parent, _attrs) => {
			_push(`<span${ssrRenderAttrs(mergeProps({ class: unref(cn)("ml-auto text-xs tracking-widest opacity-60", props.class) }, _attrs))}>`);
			ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent);
			_push(`</span>`);
		};
	}
};
var _sfc_setup$5 = _sfc_main$5.setup;
_sfc_main$5.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/dropdown-menu/DropdownMenuShortcut.vue");
	return _sfc_setup$5 ? _sfc_setup$5(props, ctx) : void 0;
};
//#endregion
//#region src/components/ui/dropdown-menu/DropdownMenuSub.vue
var _sfc_main$4 = {
	__name: "DropdownMenuSub",
	__ssrInlineRender: true,
	props: {
		defaultOpen: {
			type: Boolean,
			required: false
		},
		open: {
			type: Boolean,
			required: false
		}
	},
	emits: ["update:open"],
	setup(__props, { emit: __emit }) {
		const forwarded = useForwardPropsEmits(__props, __emit);
		return (_ctx, _push, _parent, _attrs) => {
			_push(ssrRenderComponent(unref(DropdownMenuSub), mergeProps(unref(forwarded), _attrs), {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent, _scopeId);
					else return [renderSlot(_ctx.$slots, "default")];
				}),
				_: 3
			}, _parent));
		};
	}
};
var _sfc_setup$4 = _sfc_main$4.setup;
_sfc_main$4.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/dropdown-menu/DropdownMenuSub.vue");
	return _sfc_setup$4 ? _sfc_setup$4(props, ctx) : void 0;
};
//#endregion
//#region src/components/ui/dropdown-menu/DropdownMenuSubContent.vue
var _sfc_main$3 = {
	__name: "DropdownMenuSubContent",
	__ssrInlineRender: true,
	props: {
		forceMount: {
			type: Boolean,
			required: false
		},
		loop: {
			type: Boolean,
			required: false
		},
		sideOffset: {
			type: Number,
			required: false
		},
		sideFlip: {
			type: Boolean,
			required: false
		},
		alignOffset: {
			type: Number,
			required: false
		},
		alignFlip: {
			type: Boolean,
			required: false
		},
		avoidCollisions: {
			type: Boolean,
			required: false
		},
		collisionBoundary: {
			type: null,
			required: false
		},
		collisionPadding: {
			type: [Number, Object],
			required: false
		},
		arrowPadding: {
			type: Number,
			required: false
		},
		hideShiftedArrow: {
			type: Boolean,
			required: false
		},
		sticky: {
			type: String,
			required: false
		},
		hideWhenDetached: {
			type: Boolean,
			required: false
		},
		positionStrategy: {
			type: String,
			required: false
		},
		updatePositionStrategy: {
			type: String,
			required: false
		},
		disableUpdateOnLayoutShift: {
			type: Boolean,
			required: false
		},
		prioritizePosition: {
			type: Boolean,
			required: false
		},
		reference: {
			type: null,
			required: false
		},
		asChild: {
			type: Boolean,
			required: false
		},
		as: {
			type: null,
			required: false
		},
		class: {
			type: null,
			required: false
		}
	},
	emits: [
		"escapeKeyDown",
		"pointerDownOutside",
		"focusOutside",
		"interactOutside",
		"entryFocus",
		"openAutoFocus",
		"closeAutoFocus"
	],
	setup(__props, { emit: __emit }) {
		const props = __props;
		const emits = __emit;
		const forwarded = useForwardPropsEmits(reactiveOmit(props, "class"), emits);
		return (_ctx, _push, _parent, _attrs) => {
			_push(ssrRenderComponent(unref(DropdownMenuSubContent), mergeProps(unref(forwarded), { class: unref(cn)("z-50 min-w-32 overflow-hidden rounded-md border bg-white dark:bg-zinc-950 p-1 text-zinc-900 dark:text-zinc-100 shadow-lg data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0 data-[state=closed]:zoom-out-95 data-[state=open]:zoom-in-95 data-[side=bottom]:slide-in-from-top-2 data-[side=left]:slide-in-from-right-2 data-[side=right]:slide-in-from-left-2 data-[side=top]:slide-in-from-bottom-2", props.class) }, _attrs), {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent, _scopeId);
					else return [renderSlot(_ctx.$slots, "default")];
				}),
				_: 3
			}, _parent));
		};
	}
};
var _sfc_setup$3 = _sfc_main$3.setup;
_sfc_main$3.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/dropdown-menu/DropdownMenuSubContent.vue");
	return _sfc_setup$3 ? _sfc_setup$3(props, ctx) : void 0;
};
//#endregion
//#region src/components/ui/dropdown-menu/DropdownMenuSubTrigger.vue
var _sfc_main$2 = {
	__name: "DropdownMenuSubTrigger",
	__ssrInlineRender: true,
	props: {
		disabled: {
			type: Boolean,
			required: false
		},
		textValue: {
			type: String,
			required: false
		},
		asChild: {
			type: Boolean,
			required: false
		},
		as: {
			type: null,
			required: false
		},
		class: {
			type: null,
			required: false
		}
	},
	setup(__props) {
		const props = __props;
		const forwardedProps = useForwardProps(reactiveOmit(props, "class"));
		return (_ctx, _push, _parent, _attrs) => {
			_push(ssrRenderComponent(unref(DropdownMenuSubTrigger), mergeProps(unref(forwardedProps), { class: unref(cn)("flex cursor-default select-none items-center rounded-sm px-2 py-1.5 text-sm outline-none focus:bg-accent data-[state=open]:bg-accent", props.class) }, _attrs), {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) {
						ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent, _scopeId);
						_push(ssrRenderComponent(unref(ChevronRight), { class: "ml-auto h-4 w-4" }, null, _parent, _scopeId));
					} else return [renderSlot(_ctx.$slots, "default"), createVNode(unref(ChevronRight), { class: "ml-auto h-4 w-4" })];
				}),
				_: 3
			}, _parent));
		};
	}
};
var _sfc_setup$2 = _sfc_main$2.setup;
_sfc_main$2.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/dropdown-menu/DropdownMenuSubTrigger.vue");
	return _sfc_setup$2 ? _sfc_setup$2(props, ctx) : void 0;
};
//#endregion
//#region src/components/ui/dropdown-menu/DropdownMenuTrigger.vue
var _sfc_main$1 = {
	__name: "DropdownMenuTrigger",
	__ssrInlineRender: true,
	props: {
		disabled: {
			type: Boolean,
			required: false
		},
		asChild: {
			type: Boolean,
			required: false
		},
		as: {
			type: null,
			required: false
		}
	},
	setup(__props) {
		const forwardedProps = useForwardProps(__props);
		return (_ctx, _push, _parent, _attrs) => {
			_push(ssrRenderComponent(unref(DropdownMenuTrigger), mergeProps({ class: "outline-none" }, unref(forwardedProps), _attrs), {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent, _scopeId);
					else return [renderSlot(_ctx.$slots, "default")];
				}),
				_: 3
			}, _parent));
		};
	}
};
var _sfc_setup$1 = _sfc_main$1.setup;
_sfc_main$1.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/dropdown-menu/DropdownMenuTrigger.vue");
	return _sfc_setup$1 ? _sfc_setup$1(props, ctx) : void 0;
};
//#endregion
//#region src/components/AppLayout.vue
var _sfc_main = {
	__name: "AppLayout",
	__ssrInlineRender: true,
	setup(__props) {
		const auth = useAuthStore();
		const themeStore = useThemeStore();
		const { user } = storeToRefs(auth);
		const { can } = usePermission();
		const route = useRoute();
		const router = useRouter();
		const confirmStore = useConfirmStore();
		const sidebarOpen = ref(false);
		const isAboutModalOpen = ref(false);
		const MENU_GROUPS = [
			{
				label: "Management",
				items: [
					{
						label: "Dashboard",
						icon: LayoutDashboard,
						to: "/dashboard",
						permission: null
					},
					{
						label: "User Management",
						icon: Users,
						to: "/dashboard/users",
						permission: "user.index"
					},
					{
						label: "Konten",
						icon: FileText,
						permission: null,
						children: [
							{
								label: "Produk",
								icon: ScrollText,
								to: "/dashboard/products",
								permission: "produk.index"
							},
							{
								label: "Kategori",
								icon: FileText,
								to: "/dashboard/categories",
								permission: "category.index"
							},
							{
								label: "Mutasi Stok",
								icon: ArrowLeftRight,
								to: "/dashboard/stock-mutations",
								permission: "stock-mutation.index"
							},
							{
								label: "Partner",
								icon: Users,
								to: "/dashboard/partners",
								permission: "partner.index"
							},
							{
								label: "Lokasi",
								icon: MapPin,
								to: "/dashboard/locations",
								permission: "location.index"
							},
							{
								label: "Voucer",
								icon: Ticket,
								to: "/dashboard/vouchers",
								permission: "voucher.index"
							}
						]
					}
				]
			},
			{
				label: "Access Control",
				items: [
					{
						label: "Roles",
						icon: ShieldCheck,
						to: "/dashboard/roles",
						permission: "role.index"
					},
					{
						label: "Permissions",
						icon: KeyRound,
						to: "/dashboard/permissions",
						permission: "permission.index"
					},
					{
						label: "Modul",
						icon: Zap,
						to: "/dashboard/modules",
						permission: "module.index"
					}
				]
			},
			{
				label: "Transaksi",
				items: [{
					label: "Kasir",
					icon: ShoppingCart,
					to: "/dashboard/kasir",
					permission: null
				}]
			},
			{
				label: "Master Data",
				items: [{
					label: "Audit Log",
					icon: Activity,
					to: "/dashboard/logs",
					permission: "log.index"
				}]
			}
		];
		function filterMenu(groups) {
			const userRoles = user.value?.roles || [];
			const isAdmin = userRoles.includes("ADMIN") || userRoles.some((r) => r.name === "ADMIN");
			return groups.reduce((acc, group) => {
				const filteredItems = group.items.reduce((items, item) => {
					if (item.permission && !isAdmin && !can(item.permission)) return items;
					const filtered = { ...item };
					if (filtered.children) {
						filtered.children = filterMenu([{
							label: "",
							items: filtered.children
						}])[0]?.items || [];
						if (filtered.children.length === 0) return items;
					}
					items.push(filtered);
					return items;
				}, []);
				if (filteredItems.length > 0) acc.push({
					label: group.label,
					items: filteredItems
				});
				return acc;
			}, []);
		}
		const filteredMenuGroups = computed(() => filterMenu(MENU_GROUPS));
		const expandedMenus = ref(/* @__PURE__ */ new Set());
		function isItemActive(item) {
			if (!item.to) return item.children?.some(isItemActive) ?? false;
			if (item.to === "/dashboard") return route.path === "/dashboard";
			return route.path === item.to || route.path.startsWith(item.to + "/");
		}
		function isItemExpanded(item) {
			if (!item.children) return false;
			return expandedMenus.value.has(item.label);
		}
		function expandActiveParents() {
			const s = new Set(expandedMenus.value);
			filteredMenuGroups.value.forEach((group) => {
				group.items.forEach((item) => {
					if (item.children && item.children.some((child) => isItemActive(child))) s.add(item.label);
				});
			});
			expandedMenus.value = s;
		}
		watch(() => route.path, () => {
			expandActiveParents();
			sidebarOpen.value = false;
		}, { immediate: true });
		const displayName = computed(() => user.value?.fullname || user.value?.username || "User");
		const displayEmail = computed(() => user.value?.username || "");
		const userInitial = computed(() => {
			return displayName.value.charAt(0).toUpperCase();
		});
		const currentLang = ref("EN");
		computed(() => {
			return "text-[var(--primary)]";
		});
		const themePreference = ref(localStorage.getItem("isDark") === null ? "system" : themeStore.isDark ? "dark" : "light");
		function setThemePreference(pref) {
			themePreference.value = pref;
			if (pref === "system") {
				localStorage.removeItem("isDark");
				themeStore.init();
			} else themeStore.applyTheme(themeStore.currentTheme, pref === "dark");
		}
		const isSearchOpen = ref(false);
		const searchQuery = ref("");
		const searchInputRef = ref(null);
		const activeSearchIndex = ref(0);
		const searchableMenuItems = computed(() => {
			const items = [];
			filteredMenuGroups.value.forEach((group) => {
				group.items.forEach((item) => {
					if (item.children) item.children.forEach((child) => {
						items.push({
							id: child.to || child.label,
							label: child.label,
							subtitle: `${group.label} / ${item.label}`,
							to: child.to,
							icon: child.icon
						});
					});
					else items.push({
						id: item.to || item.label,
						label: item.label,
						subtitle: group.label,
						to: item.to,
						icon: item.icon
					});
				});
			});
			return items;
		});
		const filteredSearchItems = computed(() => {
			if (!searchQuery.value) return searchableMenuItems.value;
			const query = searchQuery.value.toLowerCase();
			return searchableMenuItems.value.filter((item) => item.label.toLowerCase().includes(query) || item.subtitle.toLowerCase().includes(query));
		});
		const currentPageTitle = computed(() => {
			if (route.path === "/dashboard") return "Overview";
			const matchedItem = searchableMenuItems.value.find((item) => item.to === route.path);
			if (matchedItem) return matchedItem.label;
			const segments = route.path.split("/").filter(Boolean);
			if (segments.length > 0) {
				const last = segments[segments.length - 1];
				return last.charAt(0).toUpperCase() + last.slice(1).replace(/-/g, " ");
			}
			return "Overview";
		});
		watch(searchQuery, () => {
			activeSearchIndex.value = 0;
		});
		watch(sidebarOpen, (newVal) => {
			if (!newVal && isSearchOpen.value) closeSearch();
		});
		function openSearch() {
			sidebarOpen.value = true;
			isSearchOpen.value = true;
			searchQuery.value = "";
			activeSearchIndex.value = 0;
			setTimeout(() => {
				if (searchInputRef.value) searchInputRef.value.focus();
			}, 150);
		}
		function closeSearch() {
			isSearchOpen.value = false;
		}
		function handleGlobalKeydown(e) {
			if (e.key.toLowerCase() === "f") {
				const active = document.activeElement;
				if (!(active.tagName === "INPUT" || active.tagName === "TEXTAREA" || active.isContentEditable)) {
					e.preventDefault();
					openSearch();
				}
			} else if (e.key === "Escape" && isSearchOpen.value) closeSearch();
		}
		onMounted(() => {
			expandActiveParents();
			window.addEventListener("keydown", handleGlobalKeydown);
		});
		onBeforeUnmount(() => {
			window.removeEventListener("keydown", handleGlobalKeydown);
		});
		return (_ctx, _push, _parent, _attrs) => {
			const _component_RouterLink = resolveComponent("RouterLink");
			_push(`<div${ssrRenderAttrs(mergeProps({ class: "flex h-screen bg-background text-foreground transition-colors duration-200 overflow-hidden" }, _attrs))}>`);
			_push(ssrRenderComponent(_sfc_main$18, null, null, _parent));
			if (unref(confirmStore).isOpen) _push(ssrRenderComponent(_sfc_main$17, {
				title: unref(confirmStore).config.title,
				description: unref(confirmStore).config.description,
				"confirm-label": unref(confirmStore).config.confirmLabel,
				"cancel-label": unref(confirmStore).config.cancelLabel,
				variant: unref(confirmStore).config.variant,
				loading: unref(confirmStore).loading,
				onConfirm: unref(confirmStore).onConfirm,
				onCancel: unref(confirmStore).onCancel
			}, null, _parent));
			else _push(`<!---->`);
			if (isSearchOpen.value) _push(`<div class="fixed inset-0 z-40"></div>`);
			else _push(`<!---->`);
			if (sidebarOpen.value) _push(`<div class="fixed inset-0 bg-black/50 z-40 lg:hidden transition-opacity"></div>`);
			else _push(`<!---->`);
			_push(`<aside class="${ssrRenderClass([["fixed inset-y-0 left-0 z-50 lg:static lg:translate-x-0", sidebarOpen.value ? "translate-x-0" : "-translate-x-full"], "flex flex-col bg-white dark:bg-zinc-950 border-r border-border shrink-0 transition-transform duration-300 ease-in-out w-[280px]"])}"><div class="flex h-12 items-center px-4 border-b border-border shrink-0"><div class="flex items-center gap-2.5 overflow-hidden"><div class="w-8 h-8 bg-primary rounded-lg flex items-center justify-center shrink-0">`);
			_push(ssrRenderComponent(unref(Zap), { class: "w-5 h-5 text-primary-foreground" }, null, _parent));
			_push(`</div><span class="text-lg font-bold tracking-tight whitespace-nowrap transition-opacity duration-200"> GAPTEK </span></div></div><div class="px-3 pt-4 pb-2 shrink-0 relative z-[60]">`);
			if (!isSearchOpen.value) {
				_push(`<button class="flex items-center gap-2 w-full px-3 py-2 text-[13px] text-zinc-500 bg-zinc-100 dark:bg-zinc-900 hover:bg-zinc-200 dark:hover:bg-zinc-800 rounded-lg transition-colors outline-none border border-transparent">`);
				_push(ssrRenderComponent(unref(Search), { class: "w-4 h-4 shrink-0 text-zinc-400" }, null, _parent));
				_push(`<span class="flex-1 text-left font-medium">Find...</span><kbd class="hidden lg:inline-flex items-center justify-center h-5 px-1.5 text-[10px] font-medium rounded border border-zinc-200 dark:border-zinc-700 bg-white dark:bg-zinc-950 text-zinc-500 shadow-sm">F</kbd></button>`);
			} else {
				_push(`<div class="absolute top-4 left-3 w-[400px] max-w-[calc(100vw-24px)] bg-white dark:bg-[#09090b] rounded-xl shadow-[0_10px_40px_rgba(0,0,0,0.1)] dark:shadow-[0_10px_40px_rgba(0,0,0,0.5)] border border-zinc-200 dark:border-zinc-800 overflow-hidden flex flex-col z-[60] animate-in fade-in zoom-in-95 duration-200"><div class="flex items-center px-3 py-2.5 border-b border-zinc-200 dark:border-zinc-800 bg-zinc-50/50 dark:bg-[#09090b]">`);
				_push(ssrRenderComponent(unref(Search), { class: "w-4 h-4 text-zinc-500 shrink-0" }, null, _parent));
				_push(`<input${ssrRenderAttr("value", searchQuery.value)} type="text" placeholder="Find..." class="flex-1 bg-transparent border-none outline-none focus:ring-0 focus:outline-none px-2.5 text-[14px] text-zinc-900 dark:text-zinc-100 placeholder:text-zinc-500"><kbd class="hidden sm:inline-flex items-center justify-center h-5 px-1.5 text-[10px] font-medium rounded border border-zinc-200 dark:border-zinc-800 bg-zinc-100 dark:bg-zinc-900 text-zinc-500 cursor-pointer shadow-sm hover:bg-zinc-200 dark:hover:bg-zinc-800 transition-colors">Esc</kbd></div><div class="max-h-[350px] overflow-y-auto p-1.5 custom-scrollbar bg-white dark:bg-[#09090b]">`);
				if (filteredSearchItems.value.length === 0) _push(`<div class="py-6 text-center text-sm text-zinc-500"> No results found. </div>`);
				else _push(`<!---->`);
				_push(`<!--[-->`);
				ssrRenderList(filteredSearchItems.value, (item, index) => {
					_push(`<button class="${ssrRenderClass([activeSearchIndex.value === index ? "bg-zinc-100 dark:bg-zinc-800/80" : "hover:bg-zinc-50 dark:hover:bg-zinc-900/50", "w-full flex items-center gap-3 px-2.5 py-2.5 rounded-lg text-left transition-colors outline-none"])}"><div class="w-8 h-8 rounded-md border border-zinc-200/50 dark:border-zinc-800/50 bg-zinc-100 dark:bg-zinc-900/50 flex items-center justify-center shrink-0">`);
					ssrRenderVNode(_push, createVNode(resolveDynamicComponent(item.icon), { class: "w-4 h-4 text-zinc-600 dark:text-zinc-400" }, null), _parent);
					_push(`</div><div class="flex flex-col overflow-hidden"><span class="text-[13px] font-semibold text-zinc-900 dark:text-zinc-100 truncate leading-tight mb-0.5">${ssrInterpolate(item.label)}</span><span class="text-[11px] text-zinc-500 truncate leading-none">${ssrInterpolate(item.subtitle)}</span></div></button>`);
				});
				_push(`<!--]--></div></div>`);
			}
			_push(`</div><nav class="flex-1 overflow-y-auto py-2 px-3 custom-scrollbar"><!--[-->`);
			ssrRenderList(filteredMenuGroups.value, (group) => {
				_push(`<!--[--><div class="mb-2 mt-4 px-3"><p class="text-xs font-semibold text-zinc-400 dark:text-zinc-500 uppercase tracking-wider">${ssrInterpolate(group.label)}</p></div><div class="space-y-0.5"><!--[-->`);
				ssrRenderList(group.items, (item) => {
					_push(`<!--[-->`);
					if (!item.children) _push(ssrRenderComponent(_component_RouterLink, {
						to: item.to,
						class: ["flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-all", isItemActive(item) ? "bg-primary/10 text-primary" : "text-zinc-600 dark:text-zinc-400 hover:bg-zinc-100 dark:hover:bg-zinc-900 hover:text-zinc-900 dark:hover:text-zinc-200"]
					}, {
						default: withCtx((_, _push, _parent, _scopeId) => {
							if (_push) {
								ssrRenderVNode(_push, createVNode(resolveDynamicComponent(item.icon), { class: "w-4 h-4 shrink-0" }, null), _parent, _scopeId);
								_push(`<span${_scopeId}>${ssrInterpolate(item.label)}</span>`);
							} else return [(openBlock(), createBlock(resolveDynamicComponent(item.icon), { class: "w-4 h-4 shrink-0" })), createVNode("span", null, toDisplayString(item.label), 1)];
						}),
						_: 2
					}, _parent));
					else {
						_push(`<!--[--><button class="${ssrRenderClass([isItemActive(item) ? "bg-zinc-100 dark:bg-zinc-900 text-zinc-900 dark:text-zinc-200" : "text-zinc-600 dark:text-zinc-400 hover:bg-zinc-100 dark:hover:bg-zinc-900 hover:text-zinc-900 dark:hover:text-zinc-200", "flex w-full items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-all"])}">`);
						ssrRenderVNode(_push, createVNode(resolveDynamicComponent(item.icon), { class: "w-4 h-4 shrink-0" }, null), _parent);
						_push(`<span class="flex-1 text-left">${ssrInterpolate(item.label)}</span>`);
						_push(ssrRenderComponent(unref(ChevronRight), { class: ["w-3.5 h-3.5 transition-transform duration-200 text-zinc-400", isItemExpanded(item) ? "rotate-90" : ""] }, null, _parent));
						_push(`</button>`);
						if (isItemExpanded(item)) {
							_push(`<div class="ml-3 mt-0.5 space-y-0.5 pl-3 border-l border-zinc-200 dark:border-zinc-800"><!--[-->`);
							ssrRenderList(item.children, (child) => {
								_push(ssrRenderComponent(_component_RouterLink, {
									key: child.to || child.label,
									to: child.to,
									class: ["flex items-center gap-2.5 rounded-md px-3 py-1.5 text-sm font-medium transition-all", isItemActive(child) ? "bg-primary/10 text-primary" : "text-zinc-500 dark:text-zinc-500 hover:bg-zinc-100 dark:hover:bg-zinc-900 hover:text-zinc-900 dark:hover:text-zinc-300"]
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) {
											ssrRenderVNode(_push, createVNode(resolveDynamicComponent(child.icon), { class: "w-3.5 h-3.5 shrink-0" }, null), _parent, _scopeId);
											_push(`<span${_scopeId}>${ssrInterpolate(child.label)}</span>`);
										} else return [(openBlock(), createBlock(resolveDynamicComponent(child.icon), { class: "w-3.5 h-3.5 shrink-0" })), createVNode("span", null, toDisplayString(child.label), 1)];
									}),
									_: 2
								}, _parent));
							});
							_push(`<!--]--></div>`);
						} else _push(`<!---->`);
						_push(`<!--]-->`);
					}
					_push(`<!--]-->`);
				});
				_push(`<!--]--></div><!--]-->`);
			});
			_push(`<!--]--></nav><div class="p-2 border-t border-border shrink-0 relative z-30">`);
			_push(ssrRenderComponent(unref(_sfc_main$14), null, {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) {
						_push(ssrRenderComponent(unref(_sfc_main$1), { "as-child": "" }, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) {
									_push(`<button class="flex w-full items-center gap-2 rounded-md p-1.5 hover:bg-zinc-100 dark:hover:bg-zinc-900 transition-colors outline-none"${_scopeId}><div class="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center text-primary font-bold text-xs shrink-0 shadow-sm border border-primary/20"${_scopeId}>${ssrInterpolate(userInitial.value)}</div><div class="flex flex-col flex-1 text-left overflow-hidden"${_scopeId}><span class="text-sm font-semibold text-zinc-900 dark:text-zinc-100 truncate leading-none mb-0.5"${_scopeId}>${ssrInterpolate(displayName.value)}</span><span class="text-[10px] text-zinc-500 dark:text-zinc-500 font-medium tracking-tight uppercase"${_scopeId}>${ssrInterpolate(unref(user)?.role || "Administrator")}</span></div>`);
									_push(ssrRenderComponent(unref(ChevronDown), { class: "w-3.5 h-3.5 text-zinc-400 shrink-0" }, null, _parent, _scopeId));
									_push(`</button>`);
								} else return [createVNode("button", { class: "flex w-full items-center gap-2 rounded-md p-1.5 hover:bg-zinc-100 dark:hover:bg-zinc-900 transition-colors outline-none" }, [
									createVNode("div", { class: "w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center text-primary font-bold text-xs shrink-0 shadow-sm border border-primary/20" }, toDisplayString(userInitial.value), 1),
									createVNode("div", { class: "flex flex-col flex-1 text-left overflow-hidden" }, [createVNode("span", { class: "text-sm font-semibold text-zinc-900 dark:text-zinc-100 truncate leading-none mb-0.5" }, toDisplayString(displayName.value), 1), createVNode("span", { class: "text-[10px] text-zinc-500 dark:text-zinc-500 font-medium tracking-tight uppercase" }, toDisplayString(unref(user)?.role || "Administrator"), 1)]),
									createVNode(unref(ChevronDown), { class: "w-3.5 h-3.5 text-zinc-400 shrink-0" })
								])];
							}),
							_: 1
						}, _parent, _scopeId));
						_push(ssrRenderComponent(unref(_sfc_main$12), {
							side: "top",
							align: "start",
							"side-offset": 8,
							class: "w-[280px] p-0"
						}, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) {
									_push(`<div class="flex items-center justify-between px-3 py-3 border-b border-border"${_scopeId}><div class="flex flex-col space-y-0.5"${_scopeId}><p class="text-sm font-semibold leading-none text-zinc-900 dark:text-zinc-100"${_scopeId}>${ssrInterpolate(displayName.value)}</p><p class="text-[13px] leading-none text-zinc-500"${_scopeId}>${ssrInterpolate(displayEmail.value)}</p></div><button class="text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100 transition-colors"${_scopeId}>`);
									_push(ssrRenderComponent(unref(Settings), { class: "h-4 w-4" }, null, _parent, _scopeId));
									_push(`</button></div><div class="p-1"${_scopeId}>`);
									_push(ssrRenderComponent(unref(_sfc_main$10), {
										onClick: ($event) => unref(router).push("/"),
										class: "justify-between px-2 py-2 text-sm cursor-pointer"
									}, {
										default: withCtx((_, _push, _parent, _scopeId) => {
											if (_push) {
												_push(`<span${_scopeId}>Home Page</span>`);
												_push(ssrRenderComponent(unref(Home), { class: "h-4 w-4 text-zinc-500" }, null, _parent, _scopeId));
											} else return [createVNode("span", null, "Home Page"), createVNode(unref(Home), { class: "h-4 w-4 text-zinc-500" })];
										}),
										_: 1
									}, _parent, _scopeId));
									_push(ssrRenderComponent(unref(_sfc_main$4), null, {
										default: withCtx((_, _push, _parent, _scopeId) => {
											if (_push) {
												_push(ssrRenderComponent(unref(_sfc_main$2), { class: "flex w-full justify-between items-center px-2 py-2 text-sm cursor-pointer outline-none" }, {
													default: withCtx((_, _push, _parent, _scopeId) => {
														if (_push) _push(`<span${_scopeId}>Theme Color</span>`);
														else return [createVNode("span", null, "Theme Color")];
													}),
													_: 1
												}, _parent, _scopeId));
												_push(ssrRenderComponent(unref(DropdownMenuPortal$1), null, {
													default: withCtx((_, _push, _parent, _scopeId) => {
														if (_push) _push(ssrRenderComponent(unref(_sfc_main$3), {
															side: "right",
															align: "start",
															class: "min-w-[140px]"
														}, {
															default: withCtx((_, _push, _parent, _scopeId) => {
																if (_push) {
																	_push(`<!--[-->`);
																	ssrRenderList(unref(themeStore).themeLabels, (themeOption) => {
																		_push(ssrRenderComponent(unref(_sfc_main$10), {
																			key: themeOption.key,
																			onClick: ($event) => unref(themeStore).setTheme(themeOption.key),
																			class: "flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer"
																		}, {
																			default: withCtx((_, _push, _parent, _scopeId) => {
																				if (_push) {
																					_push(`<div class="w-3.5 h-3.5 rounded-full shrink-0 shadow-sm border border-zinc-200 dark:border-zinc-700" style="${ssrRenderStyle({ backgroundColor: themeOption.color })}"${_scopeId}></div><span class="flex-1"${_scopeId}>${ssrInterpolate(themeOption.label)}</span>`);
																					if (unref(themeStore).currentTheme === themeOption.key) _push(ssrRenderComponent(unref(Check), { class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto" }, null, _parent, _scopeId));
																					else _push(`<!---->`);
																				} else return [
																					createVNode("div", {
																						class: "w-3.5 h-3.5 rounded-full shrink-0 shadow-sm border border-zinc-200 dark:border-zinc-700",
																						style: { backgroundColor: themeOption.color }
																					}, null, 4),
																					createVNode("span", { class: "flex-1" }, toDisplayString(themeOption.label), 1),
																					unref(themeStore).currentTheme === themeOption.key ? (openBlock(), createBlock(unref(Check), {
																						key: 0,
																						class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto"
																					})) : createCommentVNode("", true)
																				];
																			}),
																			_: 2
																		}, _parent, _scopeId));
																	});
																	_push(`<!--]-->`);
																} else return [(openBlock(true), createBlock(Fragment, null, renderList(unref(themeStore).themeLabels, (themeOption) => {
																	return openBlock(), createBlock(unref(_sfc_main$10), {
																		key: themeOption.key,
																		onClick: ($event) => unref(themeStore).setTheme(themeOption.key),
																		class: "flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer"
																	}, {
																		default: withCtx(() => [
																			createVNode("div", {
																				class: "w-3.5 h-3.5 rounded-full shrink-0 shadow-sm border border-zinc-200 dark:border-zinc-700",
																				style: { backgroundColor: themeOption.color }
																			}, null, 4),
																			createVNode("span", { class: "flex-1" }, toDisplayString(themeOption.label), 1),
																			unref(themeStore).currentTheme === themeOption.key ? (openBlock(), createBlock(unref(Check), {
																				key: 0,
																				class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto"
																			})) : createCommentVNode("", true)
																		]),
																		_: 2
																	}, 1032, ["onClick"]);
																}), 128))];
															}),
															_: 1
														}, _parent, _scopeId));
														else return [createVNode(unref(_sfc_main$3), {
															side: "right",
															align: "start",
															class: "min-w-[140px]"
														}, {
															default: withCtx(() => [(openBlock(true), createBlock(Fragment, null, renderList(unref(themeStore).themeLabels, (themeOption) => {
																return openBlock(), createBlock(unref(_sfc_main$10), {
																	key: themeOption.key,
																	onClick: ($event) => unref(themeStore).setTheme(themeOption.key),
																	class: "flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer"
																}, {
																	default: withCtx(() => [
																		createVNode("div", {
																			class: "w-3.5 h-3.5 rounded-full shrink-0 shadow-sm border border-zinc-200 dark:border-zinc-700",
																			style: { backgroundColor: themeOption.color }
																		}, null, 4),
																		createVNode("span", { class: "flex-1" }, toDisplayString(themeOption.label), 1),
																		unref(themeStore).currentTheme === themeOption.key ? (openBlock(), createBlock(unref(Check), {
																			key: 0,
																			class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto"
																		})) : createCommentVNode("", true)
																	]),
																	_: 2
																}, 1032, ["onClick"]);
															}), 128))]),
															_: 1
														})];
													}),
													_: 1
												}, _parent, _scopeId));
											} else return [createVNode(unref(_sfc_main$2), { class: "flex w-full justify-between items-center px-2 py-2 text-sm cursor-pointer outline-none" }, {
												default: withCtx(() => [createVNode("span", null, "Theme Color")]),
												_: 1
											}), createVNode(unref(DropdownMenuPortal$1), null, {
												default: withCtx(() => [createVNode(unref(_sfc_main$3), {
													side: "right",
													align: "start",
													class: "min-w-[140px]"
												}, {
													default: withCtx(() => [(openBlock(true), createBlock(Fragment, null, renderList(unref(themeStore).themeLabels, (themeOption) => {
														return openBlock(), createBlock(unref(_sfc_main$10), {
															key: themeOption.key,
															onClick: ($event) => unref(themeStore).setTheme(themeOption.key),
															class: "flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer"
														}, {
															default: withCtx(() => [
																createVNode("div", {
																	class: "w-3.5 h-3.5 rounded-full shrink-0 shadow-sm border border-zinc-200 dark:border-zinc-700",
																	style: { backgroundColor: themeOption.color }
																}, null, 4),
																createVNode("span", { class: "flex-1" }, toDisplayString(themeOption.label), 1),
																unref(themeStore).currentTheme === themeOption.key ? (openBlock(), createBlock(unref(Check), {
																	key: 0,
																	class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto"
																})) : createCommentVNode("", true)
															]),
															_: 2
														}, 1032, ["onClick"]);
													}), 128))]),
													_: 1
												})]),
												_: 1
											})];
										}),
										_: 1
									}, _parent, _scopeId));
									_push(ssrRenderComponent(unref(_sfc_main$4), null, {
										default: withCtx((_, _push, _parent, _scopeId) => {
											if (_push) {
												_push(ssrRenderComponent(unref(_sfc_main$2), { class: "flex w-full justify-between items-center px-2 py-2 text-sm cursor-pointer outline-none" }, {
													default: withCtx((_, _push, _parent, _scopeId) => {
														if (_push) _push(`<span${_scopeId}>Display Mode</span>`);
														else return [createVNode("span", null, "Display Mode")];
													}),
													_: 1
												}, _parent, _scopeId));
												_push(ssrRenderComponent(unref(DropdownMenuPortal$1), null, {
													default: withCtx((_, _push, _parent, _scopeId) => {
														if (_push) _push(ssrRenderComponent(unref(_sfc_main$3), {
															side: "right",
															align: "start",
															class: "min-w-[140px]"
														}, {
															default: withCtx((_, _push, _parent, _scopeId) => {
																if (_push) {
																	_push(ssrRenderComponent(unref(_sfc_main$10), {
																		onClick: ($event) => setThemePreference("light"),
																		class: "flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer"
																	}, {
																		default: withCtx((_, _push, _parent, _scopeId) => {
																			if (_push) {
																				_push(ssrRenderComponent(unref(Sun), { class: "h-4 w-4 text-zinc-500" }, null, _parent, _scopeId));
																				_push(`<span class="flex-1"${_scopeId}>Siang (Light)</span>`);
																				if (themePreference.value === "light") _push(ssrRenderComponent(unref(Check), { class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto" }, null, _parent, _scopeId));
																				else _push(`<!---->`);
																			} else return [
																				createVNode(unref(Sun), { class: "h-4 w-4 text-zinc-500" }),
																				createVNode("span", { class: "flex-1" }, "Siang (Light)"),
																				themePreference.value === "light" ? (openBlock(), createBlock(unref(Check), {
																					key: 0,
																					class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto"
																				})) : createCommentVNode("", true)
																			];
																		}),
																		_: 1
																	}, _parent, _scopeId));
																	_push(ssrRenderComponent(unref(_sfc_main$10), {
																		onClick: ($event) => setThemePreference("dark"),
																		class: "flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer"
																	}, {
																		default: withCtx((_, _push, _parent, _scopeId) => {
																			if (_push) {
																				_push(ssrRenderComponent(unref(Moon), { class: "h-4 w-4 text-zinc-500" }, null, _parent, _scopeId));
																				_push(`<span class="flex-1"${_scopeId}>Malam (Dark)</span>`);
																				if (themePreference.value === "dark") _push(ssrRenderComponent(unref(Check), { class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto" }, null, _parent, _scopeId));
																				else _push(`<!---->`);
																			} else return [
																				createVNode(unref(Moon), { class: "h-4 w-4 text-zinc-500" }),
																				createVNode("span", { class: "flex-1" }, "Malam (Dark)"),
																				themePreference.value === "dark" ? (openBlock(), createBlock(unref(Check), {
																					key: 0,
																					class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto"
																				})) : createCommentVNode("", true)
																			];
																		}),
																		_: 1
																	}, _parent, _scopeId));
																	_push(ssrRenderComponent(unref(_sfc_main$10), {
																		onClick: ($event) => setThemePreference("system"),
																		class: "flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer"
																	}, {
																		default: withCtx((_, _push, _parent, _scopeId) => {
																			if (_push) {
																				_push(ssrRenderComponent(unref(Monitor), { class: "h-4 w-4 text-zinc-500" }, null, _parent, _scopeId));
																				_push(`<span class="flex-1"${_scopeId}>Sistem</span>`);
																				if (themePreference.value === "system") _push(ssrRenderComponent(unref(Check), { class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto" }, null, _parent, _scopeId));
																				else _push(`<!---->`);
																			} else return [
																				createVNode(unref(Monitor), { class: "h-4 w-4 text-zinc-500" }),
																				createVNode("span", { class: "flex-1" }, "Sistem"),
																				themePreference.value === "system" ? (openBlock(), createBlock(unref(Check), {
																					key: 0,
																					class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto"
																				})) : createCommentVNode("", true)
																			];
																		}),
																		_: 1
																	}, _parent, _scopeId));
																} else return [
																	createVNode(unref(_sfc_main$10), {
																		onClick: ($event) => setThemePreference("light"),
																		class: "flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer"
																	}, {
																		default: withCtx(() => [
																			createVNode(unref(Sun), { class: "h-4 w-4 text-zinc-500" }),
																			createVNode("span", { class: "flex-1" }, "Siang (Light)"),
																			themePreference.value === "light" ? (openBlock(), createBlock(unref(Check), {
																				key: 0,
																				class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto"
																			})) : createCommentVNode("", true)
																		]),
																		_: 1
																	}, 8, ["onClick"]),
																	createVNode(unref(_sfc_main$10), {
																		onClick: ($event) => setThemePreference("dark"),
																		class: "flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer"
																	}, {
																		default: withCtx(() => [
																			createVNode(unref(Moon), { class: "h-4 w-4 text-zinc-500" }),
																			createVNode("span", { class: "flex-1" }, "Malam (Dark)"),
																			themePreference.value === "dark" ? (openBlock(), createBlock(unref(Check), {
																				key: 0,
																				class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto"
																			})) : createCommentVNode("", true)
																		]),
																		_: 1
																	}, 8, ["onClick"]),
																	createVNode(unref(_sfc_main$10), {
																		onClick: ($event) => setThemePreference("system"),
																		class: "flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer"
																	}, {
																		default: withCtx(() => [
																			createVNode(unref(Monitor), { class: "h-4 w-4 text-zinc-500" }),
																			createVNode("span", { class: "flex-1" }, "Sistem"),
																			themePreference.value === "system" ? (openBlock(), createBlock(unref(Check), {
																				key: 0,
																				class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto"
																			})) : createCommentVNode("", true)
																		]),
																		_: 1
																	}, 8, ["onClick"])
																];
															}),
															_: 1
														}, _parent, _scopeId));
														else return [createVNode(unref(_sfc_main$3), {
															side: "right",
															align: "start",
															class: "min-w-[140px]"
														}, {
															default: withCtx(() => [
																createVNode(unref(_sfc_main$10), {
																	onClick: ($event) => setThemePreference("light"),
																	class: "flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer"
																}, {
																	default: withCtx(() => [
																		createVNode(unref(Sun), { class: "h-4 w-4 text-zinc-500" }),
																		createVNode("span", { class: "flex-1" }, "Siang (Light)"),
																		themePreference.value === "light" ? (openBlock(), createBlock(unref(Check), {
																			key: 0,
																			class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto"
																		})) : createCommentVNode("", true)
																	]),
																	_: 1
																}, 8, ["onClick"]),
																createVNode(unref(_sfc_main$10), {
																	onClick: ($event) => setThemePreference("dark"),
																	class: "flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer"
																}, {
																	default: withCtx(() => [
																		createVNode(unref(Moon), { class: "h-4 w-4 text-zinc-500" }),
																		createVNode("span", { class: "flex-1" }, "Malam (Dark)"),
																		themePreference.value === "dark" ? (openBlock(), createBlock(unref(Check), {
																			key: 0,
																			class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto"
																		})) : createCommentVNode("", true)
																	]),
																	_: 1
																}, 8, ["onClick"]),
																createVNode(unref(_sfc_main$10), {
																	onClick: ($event) => setThemePreference("system"),
																	class: "flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer"
																}, {
																	default: withCtx(() => [
																		createVNode(unref(Monitor), { class: "h-4 w-4 text-zinc-500" }),
																		createVNode("span", { class: "flex-1" }, "Sistem"),
																		themePreference.value === "system" ? (openBlock(), createBlock(unref(Check), {
																			key: 0,
																			class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto"
																		})) : createCommentVNode("", true)
																	]),
																	_: 1
																}, 8, ["onClick"])
															]),
															_: 1
														})];
													}),
													_: 1
												}, _parent, _scopeId));
											} else return [createVNode(unref(_sfc_main$2), { class: "flex w-full justify-between items-center px-2 py-2 text-sm cursor-pointer outline-none" }, {
												default: withCtx(() => [createVNode("span", null, "Display Mode")]),
												_: 1
											}), createVNode(unref(DropdownMenuPortal$1), null, {
												default: withCtx(() => [createVNode(unref(_sfc_main$3), {
													side: "right",
													align: "start",
													class: "min-w-[140px]"
												}, {
													default: withCtx(() => [
														createVNode(unref(_sfc_main$10), {
															onClick: ($event) => setThemePreference("light"),
															class: "flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer"
														}, {
															default: withCtx(() => [
																createVNode(unref(Sun), { class: "h-4 w-4 text-zinc-500" }),
																createVNode("span", { class: "flex-1" }, "Siang (Light)"),
																themePreference.value === "light" ? (openBlock(), createBlock(unref(Check), {
																	key: 0,
																	class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto"
																})) : createCommentVNode("", true)
															]),
															_: 1
														}, 8, ["onClick"]),
														createVNode(unref(_sfc_main$10), {
															onClick: ($event) => setThemePreference("dark"),
															class: "flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer"
														}, {
															default: withCtx(() => [
																createVNode(unref(Moon), { class: "h-4 w-4 text-zinc-500" }),
																createVNode("span", { class: "flex-1" }, "Malam (Dark)"),
																themePreference.value === "dark" ? (openBlock(), createBlock(unref(Check), {
																	key: 0,
																	class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto"
																})) : createCommentVNode("", true)
															]),
															_: 1
														}, 8, ["onClick"]),
														createVNode(unref(_sfc_main$10), {
															onClick: ($event) => setThemePreference("system"),
															class: "flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer"
														}, {
															default: withCtx(() => [
																createVNode(unref(Monitor), { class: "h-4 w-4 text-zinc-500" }),
																createVNode("span", { class: "flex-1" }, "Sistem"),
																themePreference.value === "system" ? (openBlock(), createBlock(unref(Check), {
																	key: 0,
																	class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto"
																})) : createCommentVNode("", true)
															]),
															_: 1
														}, 8, ["onClick"])
													]),
													_: 1
												})]),
												_: 1
											})];
										}),
										_: 1
									}, _parent, _scopeId));
									_push(ssrRenderComponent(unref(_sfc_main$10), {
										onClick: ($event) => unref(auth).logout(),
										class: "justify-between px-2 py-2 text-sm cursor-pointer text-zinc-900 dark:text-zinc-100"
									}, {
										default: withCtx((_, _push, _parent, _scopeId) => {
											if (_push) {
												_push(`<span${_scopeId}>Log Out</span>`);
												_push(ssrRenderComponent(unref(LogOut), { class: "h-4 w-4 text-zinc-500" }, null, _parent, _scopeId));
											} else return [createVNode("span", null, "Log Out"), createVNode(unref(LogOut), { class: "h-4 w-4 text-zinc-500" })];
										}),
										_: 1
									}, _parent, _scopeId));
									_push(`</div><div class="border-t border-border bg-zinc-50/50 dark:bg-zinc-900/50 px-3 py-2.5 flex items-center justify-between rounded-b-md"${_scopeId}><div class="flex flex-col"${_scopeId}><span class="text-xs font-medium text-zinc-500"${_scopeId}>Platform Status</span><span class="text-[13px] text-zinc-900 dark:text-zinc-100"${_scopeId}>All systems normal.</span></div><div class="h-2.5 w-2.5 rounded-full bg-blue-500 shadow-[0_0_8px_rgba(59,130,246,0.5)]"${_scopeId}></div></div>`);
								} else return [
									createVNode("div", { class: "flex items-center justify-between px-3 py-3 border-b border-border" }, [createVNode("div", { class: "flex flex-col space-y-0.5" }, [createVNode("p", { class: "text-sm font-semibold leading-none text-zinc-900 dark:text-zinc-100" }, toDisplayString(displayName.value), 1), createVNode("p", { class: "text-[13px] leading-none text-zinc-500" }, toDisplayString(displayEmail.value), 1)]), createVNode("button", {
										onClick: ($event) => unref(router).push("/dashboard/profile"),
										class: "text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100 transition-colors"
									}, [createVNode(unref(Settings), { class: "h-4 w-4" })], 8, ["onClick"])]),
									createVNode("div", { class: "p-1" }, [
										createVNode(unref(_sfc_main$10), {
											onClick: ($event) => unref(router).push("/"),
											class: "justify-between px-2 py-2 text-sm cursor-pointer"
										}, {
											default: withCtx(() => [createVNode("span", null, "Home Page"), createVNode(unref(Home), { class: "h-4 w-4 text-zinc-500" })]),
											_: 1
										}, 8, ["onClick"]),
										createVNode(unref(_sfc_main$4), null, {
											default: withCtx(() => [createVNode(unref(_sfc_main$2), { class: "flex w-full justify-between items-center px-2 py-2 text-sm cursor-pointer outline-none" }, {
												default: withCtx(() => [createVNode("span", null, "Theme Color")]),
												_: 1
											}), createVNode(unref(DropdownMenuPortal$1), null, {
												default: withCtx(() => [createVNode(unref(_sfc_main$3), {
													side: "right",
													align: "start",
													class: "min-w-[140px]"
												}, {
													default: withCtx(() => [(openBlock(true), createBlock(Fragment, null, renderList(unref(themeStore).themeLabels, (themeOption) => {
														return openBlock(), createBlock(unref(_sfc_main$10), {
															key: themeOption.key,
															onClick: ($event) => unref(themeStore).setTheme(themeOption.key),
															class: "flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer"
														}, {
															default: withCtx(() => [
																createVNode("div", {
																	class: "w-3.5 h-3.5 rounded-full shrink-0 shadow-sm border border-zinc-200 dark:border-zinc-700",
																	style: { backgroundColor: themeOption.color }
																}, null, 4),
																createVNode("span", { class: "flex-1" }, toDisplayString(themeOption.label), 1),
																unref(themeStore).currentTheme === themeOption.key ? (openBlock(), createBlock(unref(Check), {
																	key: 0,
																	class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto"
																})) : createCommentVNode("", true)
															]),
															_: 2
														}, 1032, ["onClick"]);
													}), 128))]),
													_: 1
												})]),
												_: 1
											})]),
											_: 1
										}),
										createVNode(unref(_sfc_main$4), null, {
											default: withCtx(() => [createVNode(unref(_sfc_main$2), { class: "flex w-full justify-between items-center px-2 py-2 text-sm cursor-pointer outline-none" }, {
												default: withCtx(() => [createVNode("span", null, "Display Mode")]),
												_: 1
											}), createVNode(unref(DropdownMenuPortal$1), null, {
												default: withCtx(() => [createVNode(unref(_sfc_main$3), {
													side: "right",
													align: "start",
													class: "min-w-[140px]"
												}, {
													default: withCtx(() => [
														createVNode(unref(_sfc_main$10), {
															onClick: ($event) => setThemePreference("light"),
															class: "flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer"
														}, {
															default: withCtx(() => [
																createVNode(unref(Sun), { class: "h-4 w-4 text-zinc-500" }),
																createVNode("span", { class: "flex-1" }, "Siang (Light)"),
																themePreference.value === "light" ? (openBlock(), createBlock(unref(Check), {
																	key: 0,
																	class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto"
																})) : createCommentVNode("", true)
															]),
															_: 1
														}, 8, ["onClick"]),
														createVNode(unref(_sfc_main$10), {
															onClick: ($event) => setThemePreference("dark"),
															class: "flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer"
														}, {
															default: withCtx(() => [
																createVNode(unref(Moon), { class: "h-4 w-4 text-zinc-500" }),
																createVNode("span", { class: "flex-1" }, "Malam (Dark)"),
																themePreference.value === "dark" ? (openBlock(), createBlock(unref(Check), {
																	key: 0,
																	class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto"
																})) : createCommentVNode("", true)
															]),
															_: 1
														}, 8, ["onClick"]),
														createVNode(unref(_sfc_main$10), {
															onClick: ($event) => setThemePreference("system"),
															class: "flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer"
														}, {
															default: withCtx(() => [
																createVNode(unref(Monitor), { class: "h-4 w-4 text-zinc-500" }),
																createVNode("span", { class: "flex-1" }, "Sistem"),
																themePreference.value === "system" ? (openBlock(), createBlock(unref(Check), {
																	key: 0,
																	class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto"
																})) : createCommentVNode("", true)
															]),
															_: 1
														}, 8, ["onClick"])
													]),
													_: 1
												})]),
												_: 1
											})]),
											_: 1
										}),
										createVNode(unref(_sfc_main$10), {
											onClick: ($event) => unref(auth).logout(),
											class: "justify-between px-2 py-2 text-sm cursor-pointer text-zinc-900 dark:text-zinc-100"
										}, {
											default: withCtx(() => [createVNode("span", null, "Log Out"), createVNode(unref(LogOut), { class: "h-4 w-4 text-zinc-500" })]),
											_: 1
										}, 8, ["onClick"])
									]),
									createVNode("div", { class: "border-t border-border bg-zinc-50/50 dark:bg-zinc-900/50 px-3 py-2.5 flex items-center justify-between rounded-b-md" }, [createVNode("div", { class: "flex flex-col" }, [createVNode("span", { class: "text-xs font-medium text-zinc-500" }, "Platform Status"), createVNode("span", { class: "text-[13px] text-zinc-900 dark:text-zinc-100" }, "All systems normal.")]), createVNode("div", { class: "h-2.5 w-2.5 rounded-full bg-blue-500 shadow-[0_0_8px_rgba(59,130,246,0.5)]" })])
								];
							}),
							_: 1
						}, _parent, _scopeId));
					} else return [createVNode(unref(_sfc_main$1), { "as-child": "" }, {
						default: withCtx(() => [createVNode("button", { class: "flex w-full items-center gap-2 rounded-md p-1.5 hover:bg-zinc-100 dark:hover:bg-zinc-900 transition-colors outline-none" }, [
							createVNode("div", { class: "w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center text-primary font-bold text-xs shrink-0 shadow-sm border border-primary/20" }, toDisplayString(userInitial.value), 1),
							createVNode("div", { class: "flex flex-col flex-1 text-left overflow-hidden" }, [createVNode("span", { class: "text-sm font-semibold text-zinc-900 dark:text-zinc-100 truncate leading-none mb-0.5" }, toDisplayString(displayName.value), 1), createVNode("span", { class: "text-[10px] text-zinc-500 dark:text-zinc-500 font-medium tracking-tight uppercase" }, toDisplayString(unref(user)?.role || "Administrator"), 1)]),
							createVNode(unref(ChevronDown), { class: "w-3.5 h-3.5 text-zinc-400 shrink-0" })
						])]),
						_: 1
					}), createVNode(unref(_sfc_main$12), {
						side: "top",
						align: "start",
						"side-offset": 8,
						class: "w-[280px] p-0"
					}, {
						default: withCtx(() => [
							createVNode("div", { class: "flex items-center justify-between px-3 py-3 border-b border-border" }, [createVNode("div", { class: "flex flex-col space-y-0.5" }, [createVNode("p", { class: "text-sm font-semibold leading-none text-zinc-900 dark:text-zinc-100" }, toDisplayString(displayName.value), 1), createVNode("p", { class: "text-[13px] leading-none text-zinc-500" }, toDisplayString(displayEmail.value), 1)]), createVNode("button", {
								onClick: ($event) => unref(router).push("/dashboard/profile"),
								class: "text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100 transition-colors"
							}, [createVNode(unref(Settings), { class: "h-4 w-4" })], 8, ["onClick"])]),
							createVNode("div", { class: "p-1" }, [
								createVNode(unref(_sfc_main$10), {
									onClick: ($event) => unref(router).push("/"),
									class: "justify-between px-2 py-2 text-sm cursor-pointer"
								}, {
									default: withCtx(() => [createVNode("span", null, "Home Page"), createVNode(unref(Home), { class: "h-4 w-4 text-zinc-500" })]),
									_: 1
								}, 8, ["onClick"]),
								createVNode(unref(_sfc_main$4), null, {
									default: withCtx(() => [createVNode(unref(_sfc_main$2), { class: "flex w-full justify-between items-center px-2 py-2 text-sm cursor-pointer outline-none" }, {
										default: withCtx(() => [createVNode("span", null, "Theme Color")]),
										_: 1
									}), createVNode(unref(DropdownMenuPortal$1), null, {
										default: withCtx(() => [createVNode(unref(_sfc_main$3), {
											side: "right",
											align: "start",
											class: "min-w-[140px]"
										}, {
											default: withCtx(() => [(openBlock(true), createBlock(Fragment, null, renderList(unref(themeStore).themeLabels, (themeOption) => {
												return openBlock(), createBlock(unref(_sfc_main$10), {
													key: themeOption.key,
													onClick: ($event) => unref(themeStore).setTheme(themeOption.key),
													class: "flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer"
												}, {
													default: withCtx(() => [
														createVNode("div", {
															class: "w-3.5 h-3.5 rounded-full shrink-0 shadow-sm border border-zinc-200 dark:border-zinc-700",
															style: { backgroundColor: themeOption.color }
														}, null, 4),
														createVNode("span", { class: "flex-1" }, toDisplayString(themeOption.label), 1),
														unref(themeStore).currentTheme === themeOption.key ? (openBlock(), createBlock(unref(Check), {
															key: 0,
															class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto"
														})) : createCommentVNode("", true)
													]),
													_: 2
												}, 1032, ["onClick"]);
											}), 128))]),
											_: 1
										})]),
										_: 1
									})]),
									_: 1
								}),
								createVNode(unref(_sfc_main$4), null, {
									default: withCtx(() => [createVNode(unref(_sfc_main$2), { class: "flex w-full justify-between items-center px-2 py-2 text-sm cursor-pointer outline-none" }, {
										default: withCtx(() => [createVNode("span", null, "Display Mode")]),
										_: 1
									}), createVNode(unref(DropdownMenuPortal$1), null, {
										default: withCtx(() => [createVNode(unref(_sfc_main$3), {
											side: "right",
											align: "start",
											class: "min-w-[140px]"
										}, {
											default: withCtx(() => [
												createVNode(unref(_sfc_main$10), {
													onClick: ($event) => setThemePreference("light"),
													class: "flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer"
												}, {
													default: withCtx(() => [
														createVNode(unref(Sun), { class: "h-4 w-4 text-zinc-500" }),
														createVNode("span", { class: "flex-1" }, "Siang (Light)"),
														themePreference.value === "light" ? (openBlock(), createBlock(unref(Check), {
															key: 0,
															class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto"
														})) : createCommentVNode("", true)
													]),
													_: 1
												}, 8, ["onClick"]),
												createVNode(unref(_sfc_main$10), {
													onClick: ($event) => setThemePreference("dark"),
													class: "flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer"
												}, {
													default: withCtx(() => [
														createVNode(unref(Moon), { class: "h-4 w-4 text-zinc-500" }),
														createVNode("span", { class: "flex-1" }, "Malam (Dark)"),
														themePreference.value === "dark" ? (openBlock(), createBlock(unref(Check), {
															key: 0,
															class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto"
														})) : createCommentVNode("", true)
													]),
													_: 1
												}, 8, ["onClick"]),
												createVNode(unref(_sfc_main$10), {
													onClick: ($event) => setThemePreference("system"),
													class: "flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer"
												}, {
													default: withCtx(() => [
														createVNode(unref(Monitor), { class: "h-4 w-4 text-zinc-500" }),
														createVNode("span", { class: "flex-1" }, "Sistem"),
														themePreference.value === "system" ? (openBlock(), createBlock(unref(Check), {
															key: 0,
															class: "w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto"
														})) : createCommentVNode("", true)
													]),
													_: 1
												}, 8, ["onClick"])
											]),
											_: 1
										})]),
										_: 1
									})]),
									_: 1
								}),
								createVNode(unref(_sfc_main$10), {
									onClick: ($event) => unref(auth).logout(),
									class: "justify-between px-2 py-2 text-sm cursor-pointer text-zinc-900 dark:text-zinc-100"
								}, {
									default: withCtx(() => [createVNode("span", null, "Log Out"), createVNode(unref(LogOut), { class: "h-4 w-4 text-zinc-500" })]),
									_: 1
								}, 8, ["onClick"])
							]),
							createVNode("div", { class: "border-t border-border bg-zinc-50/50 dark:bg-zinc-900/50 px-3 py-2.5 flex items-center justify-between rounded-b-md" }, [createVNode("div", { class: "flex flex-col" }, [createVNode("span", { class: "text-xs font-medium text-zinc-500" }, "Platform Status"), createVNode("span", { class: "text-[13px] text-zinc-900 dark:text-zinc-100" }, "All systems normal.")]), createVNode("div", { class: "h-2.5 w-2.5 rounded-full bg-blue-500 shadow-[0_0_8px_rgba(59,130,246,0.5)]" })])
						]),
						_: 1
					})];
				}),
				_: 1
			}, _parent));
			_push(`</div></aside><div class="flex-1 flex flex-col min-w-0 overflow-hidden"><header class="relative flex h-12 shrink-0 items-center justify-between bg-white dark:bg-zinc-950 border-b border-border px-4"><div class="flex items-center gap-4 w-1/3"><button class="p-1.5 rounded-md hover:bg-zinc-100 dark:hover:bg-zinc-900 text-zinc-500 transition-colors lg:hidden" title="Toggle sidebar">`);
			_push(ssrRenderComponent(unref(PanelLeftOpen), { class: "w-4 h-4" }, null, _parent));
			_push(`</button></div><div class="absolute left-1/2 -translate-x-1/2 text-[14px] font-semibold text-zinc-800 dark:text-zinc-200 truncate max-w-[50%] text-center pointer-events-none">${ssrInterpolate(currentPageTitle.value)}</div><div class="flex items-center justify-end gap-3 w-1/3"><div class="flex items-center gap-2 text-[11px] font-medium tracking-tight"><button class="${ssrRenderClass([currentLang.value === "ID" ? "font-semibold text-primary" : "text-zinc-400 hover:text-zinc-600", "transition-colors px-1"])}"> ID </button><span class="text-zinc-300 dark:text-zinc-700">|</span><button class="${ssrRenderClass([currentLang.value === "EN" ? "font-semibold text-primary" : "text-zinc-400 hover:text-zinc-600", "transition-colors px-1"])}"> EN </button></div><button class="p-1.5 rounded-lg hover:bg-zinc-100 dark:hover:bg-zinc-900 text-zinc-500 transition-colors" title="Help">`);
			_push(ssrRenderComponent(unref(HelpCircle), { class: "w-4 h-4" }, null, _parent));
			_push(`</button></div></header><main class="flex-1 overflow-y-auto custom-scrollbar bg-zinc-50/50 dark:bg-zinc-950"><div class="p-5">`);
			ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent);
			_push(`</div></main></div>`);
			_push(ssrRenderComponent(_sfc_main$16, {
				"is-open": isAboutModalOpen.value,
				onClose: ($event) => isAboutModalOpen.value = false
			}, null, _parent));
			_push(`</div>`);
		};
	}
};
var _sfc_setup = _sfc_main.setup;
_sfc_main.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/AppLayout.vue");
	return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
//#endregion
export { _sfc_main$12 as a, _sfc_main$16 as c, usePermission as d, _sfc_main$10 as i, useConfirmStore as l, _sfc_main$1 as n, _sfc_main$14 as o, _sfc_main$6 as r, _sfc_main$15 as s, _sfc_main as t, useToastStore as u };
