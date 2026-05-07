import { s as _sfc_main$1 } from "./AppLayout-D1IhsFmL.js";
import { computed, createTextVNode, createVNode, mergeProps, toDisplayString, unref, useSSRContext, withCtx } from "vue";
import { ssrInterpolate, ssrRenderAttr, ssrRenderAttrs, ssrRenderComponent, ssrRenderList } from "vue/server-renderer";
import { ChevronDown, ChevronLeft, ChevronRight } from "lucide-vue-next";
//#region src/components/ui/DataTablePagination.vue
var _sfc_main = {
	__name: "DataTablePagination",
	__ssrInlineRender: true,
	props: {
		page: {
			type: Number,
			required: true
		},
		pageSize: {
			type: Number,
			required: true
		},
		total: {
			type: Number,
			required: true
		}
	},
	emits: ["update:page", "update:pageSize"],
	setup(__props, { emit: __emit }) {
		const props = __props;
		const emit = __emit;
		const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)));
		function prevPage() {
			if (props.page > 1) emit("update:page", props.page - 1);
		}
		function nextPage() {
			if (props.page < totalPages.value) emit("update:page", props.page + 1);
		}
		const pageNumbers = computed(() => {
			const pages = [];
			const maxVisible = 5;
			let start = Math.max(1, props.page - Math.floor(maxVisible / 2));
			let end = Math.min(totalPages.value, start + maxVisible - 1);
			if (end - start < maxVisible - 1) start = Math.max(1, end - maxVisible + 1);
			for (let i = start; i <= end; i++) pages.push(i);
			return pages;
		});
		return (_ctx, _push, _parent, _attrs) => {
			_push(`<div${ssrRenderAttrs(mergeProps({ class: "flex flex-col sm:flex-row items-center justify-between gap-4 px-6 py-5 border-t border-zinc-100 dark:border-zinc-800" }, _attrs))}><div class="flex items-center gap-3"><div class="relative group"><div class="flex h-9 w-[72px] items-center justify-center gap-1.5 rounded-lg border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-950 text-zinc-900 dark:text-zinc-100 text-sm font-medium group-focus-within:ring-2 group-focus-within:ring-primary/20"><span>${ssrInterpolate(__props.pageSize)}</span>`);
			_push(ssrRenderComponent(unref(ChevronDown), { class: "h-4 w-4 text-zinc-500" }, null, _parent));
			_push(`</div><select${ssrRenderAttr("value", __props.pageSize)} class="absolute inset-0 w-full h-full opacity-0 cursor-pointer"><!--[-->`);
			ssrRenderList([
				10,
				20,
				30,
				50,
				100
			], (size) => {
				_push(`<option${ssrRenderAttr("value", size)} class="text-zinc-900 dark:text-zinc-100 dark:bg-zinc-950">${ssrInterpolate(size)}</option>`);
			});
			_push(`<!--]--></select></div><span class="text-sm font-medium text-zinc-500">Rows per page</span></div><div class="flex items-center gap-4 text-xs font-medium"><span class="text-zinc-500"> Page <span class="text-zinc-900 dark:text-zinc-100">${ssrInterpolate(__props.page)}</span> of <span class="text-zinc-900 dark:text-zinc-100">${ssrInterpolate(totalPages.value)}</span></span><div class="flex items-center gap-1">`);
			_push(ssrRenderComponent(unref(_sfc_main$1), {
				variant: "outline",
				size: "icon",
				disabled: __props.page === 1,
				onClick: prevPage,
				class: "h-8 w-8 border-zinc-200 dark:border-zinc-800 hover:bg-zinc-50"
			}, {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) _push(ssrRenderComponent(unref(ChevronLeft), { class: "h-3.5 w-3.5 text-zinc-600" }, null, _parent, _scopeId));
					else return [createVNode(unref(ChevronLeft), { class: "h-3.5 w-3.5 text-zinc-600" })];
				}),
				_: 1
			}, _parent));
			_push(`<div class="flex items-center gap-1"><!--[-->`);
			ssrRenderList(pageNumbers.value, (pageNum) => {
				_push(ssrRenderComponent(unref(_sfc_main$1), {
					variant: pageNum === __props.page ? "default" : "outline",
					onClick: ($event) => emit("update:page", pageNum),
					class: ["h-8 w-8 p-0 text-[11px] font-bold shadow-none", pageNum === __props.page ? "bg-primary text-primary-foreground hover:bg-primary/90 border-none" : "border-zinc-200 dark:border-zinc-800 text-zinc-600 hover:bg-zinc-50"]
				}, {
					default: withCtx((_, _push, _parent, _scopeId) => {
						if (_push) _push(`${ssrInterpolate(pageNum)}`);
						else return [createTextVNode(toDisplayString(pageNum), 1)];
					}),
					_: 2
				}, _parent));
			});
			_push(`<!--]--></div>`);
			_push(ssrRenderComponent(unref(_sfc_main$1), {
				variant: "outline",
				size: "icon",
				disabled: __props.page === totalPages.value,
				onClick: nextPage,
				class: "h-8 w-8 border-zinc-200 dark:border-zinc-800 hover:bg-zinc-50"
			}, {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) _push(ssrRenderComponent(unref(ChevronRight), { class: "h-3.5 w-3.5 text-zinc-600" }, null, _parent, _scopeId));
					else return [createVNode(unref(ChevronRight), { class: "h-3.5 w-3.5 text-zinc-600" })];
				}),
				_: 1
			}, _parent));
			_push(`</div></div></div>`);
		};
	}
};
var _sfc_setup = _sfc_main.setup;
_sfc_main.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/DataTablePagination.vue");
	return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
//#endregion
export { _sfc_main as t };
