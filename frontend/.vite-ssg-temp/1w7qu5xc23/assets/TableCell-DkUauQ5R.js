import { n as cn } from "./Button-Bj0EF1Kv.js";
import { computed, mergeProps, unref, useSSRContext } from "vue";
import { ssrRenderAttrs, ssrRenderSlot } from "vue/server-renderer";
//#region src/components/ui/Table.vue
var _sfc_main$5 = {
	__name: "Table",
	__ssrInlineRender: true,
	props: { class: {
		type: String,
		default: ""
	} },
	setup(__props) {
		const props = __props;
		const delegatedProps = computed(() => {
			const { class: _, ...rest } = props;
			return rest;
		});
		return (_ctx, _push, _parent, _attrs) => {
			_push(`<div${ssrRenderAttrs(mergeProps({ class: "w-full overflow-auto" }, _attrs))}><table${ssrRenderAttrs(mergeProps(delegatedProps.value, { class: unref(cn)("w-full caption-bottom text-sm", props.class) }))}>`);
			ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent);
			_push(`</table></div>`);
		};
	}
};
var _sfc_setup$5 = _sfc_main$5.setup;
_sfc_main$5.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/Table.vue");
	return _sfc_setup$5 ? _sfc_setup$5(props, ctx) : void 0;
};
//#endregion
//#region src/components/ui/TableHeader.vue
var _sfc_main$4 = {
	__name: "TableHeader",
	__ssrInlineRender: true,
	props: { class: {
		type: String,
		default: ""
	} },
	setup(__props) {
		const props = __props;
		const delegatedProps = computed(() => {
			const { class: _, ...rest } = props;
			return rest;
		});
		return (_ctx, _push, _parent, _attrs) => {
			_push(`<thead${ssrRenderAttrs(mergeProps(delegatedProps.value, { class: unref(cn)("[&_tr]:border-b", props.class) }, _attrs))}>`);
			ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent);
			_push(`</thead>`);
		};
	}
};
var _sfc_setup$4 = _sfc_main$4.setup;
_sfc_main$4.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/TableHeader.vue");
	return _sfc_setup$4 ? _sfc_setup$4(props, ctx) : void 0;
};
//#endregion
//#region src/components/ui/TableBody.vue
var _sfc_main$3 = {
	__name: "TableBody",
	__ssrInlineRender: true,
	props: { class: {
		type: String,
		default: ""
	} },
	setup(__props) {
		const props = __props;
		const delegatedProps = computed(() => {
			const { class: _, ...rest } = props;
			return rest;
		});
		return (_ctx, _push, _parent, _attrs) => {
			_push(`<tbody${ssrRenderAttrs(mergeProps(delegatedProps.value, { class: unref(cn)("[&_tr:last-child]:border-0", props.class) }, _attrs))}>`);
			ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent);
			_push(`</tbody>`);
		};
	}
};
var _sfc_setup$3 = _sfc_main$3.setup;
_sfc_main$3.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/TableBody.vue");
	return _sfc_setup$3 ? _sfc_setup$3(props, ctx) : void 0;
};
//#endregion
//#region src/components/ui/TableRow.vue
var _sfc_main$2 = {
	__name: "TableRow",
	__ssrInlineRender: true,
	props: { class: {
		type: String,
		default: ""
	} },
	setup(__props) {
		const props = __props;
		const delegatedProps = computed(() => {
			const { class: _, ...rest } = props;
			return rest;
		});
		return (_ctx, _push, _parent, _attrs) => {
			_push(`<tr${ssrRenderAttrs(mergeProps(delegatedProps.value, { class: unref(cn)("border-b border-border transition-colors hover:bg-muted/50 data-[state=selected]:bg-muted", props.class) }, _attrs))}>`);
			ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent);
			_push(`</tr>`);
		};
	}
};
var _sfc_setup$2 = _sfc_main$2.setup;
_sfc_main$2.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/TableRow.vue");
	return _sfc_setup$2 ? _sfc_setup$2(props, ctx) : void 0;
};
//#endregion
//#region src/components/ui/TableHead.vue
var _sfc_main$1 = {
	__name: "TableHead",
	__ssrInlineRender: true,
	props: { class: {
		type: String,
		default: ""
	} },
	setup(__props) {
		const props = __props;
		const delegatedProps = computed(() => {
			const { class: _, ...rest } = props;
			return rest;
		});
		return (_ctx, _push, _parent, _attrs) => {
			_push(`<th${ssrRenderAttrs(mergeProps(delegatedProps.value, { class: unref(cn)("h-8 px-4 text-left align-middle font-semibold text-zinc-500 text-[11px] uppercase tracking-wider [&:has([role=checkbox])]:pr-0", props.class) }, _attrs))}>`);
			ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent);
			_push(`</th>`);
		};
	}
};
var _sfc_setup$1 = _sfc_main$1.setup;
_sfc_main$1.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/TableHead.vue");
	return _sfc_setup$1 ? _sfc_setup$1(props, ctx) : void 0;
};
//#endregion
//#region src/components/ui/TableCell.vue
var _sfc_main = {
	__name: "TableCell",
	__ssrInlineRender: true,
	props: { class: {
		type: String,
		default: ""
	} },
	setup(__props) {
		const props = __props;
		const delegatedProps = computed(() => {
			const { class: _, ...rest } = props;
			return rest;
		});
		return (_ctx, _push, _parent, _attrs) => {
			_push(`<td${ssrRenderAttrs(mergeProps(delegatedProps.value, { class: unref(cn)("px-4 py-2.5 align-middle text-xs [&:has([role=checkbox])]:pr-0", props.class) }, _attrs))}>`);
			ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent);
			_push(`</td>`);
		};
	}
};
var _sfc_setup = _sfc_main.setup;
_sfc_main.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/TableCell.vue");
	return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
//#endregion
export { _sfc_main$4 as a, _sfc_main$3 as i, _sfc_main$1 as n, _sfc_main$5 as o, _sfc_main$2 as r, _sfc_main as t };
