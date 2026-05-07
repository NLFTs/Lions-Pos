import { n as cn } from "./Button-Bj0EF1Kv.js";
import { computed, mergeProps, useSSRContext } from "vue";
import { ssrRenderAttrs, ssrRenderSlot } from "vue/server-renderer";
//#region src/components/ui/CardHeader.vue
var _sfc_main$1 = {
	__name: "CardHeader",
	__ssrInlineRender: true,
	props: { class: String },
	setup(__props) {
		const props = __props;
		const classes = computed(() => cn("flex flex-col space-y-1.5 p-6", props.class));
		return (_ctx, _push, _parent, _attrs) => {
			_push(`<div${ssrRenderAttrs(mergeProps({ class: classes.value }, _attrs))}>`);
			ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent);
			_push(`</div>`);
		};
	}
};
var _sfc_setup$1 = _sfc_main$1.setup;
_sfc_main$1.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/CardHeader.vue");
	return _sfc_setup$1 ? _sfc_setup$1(props, ctx) : void 0;
};
//#endregion
//#region src/components/ui/CardTitle.vue
var _sfc_main = {
	__name: "CardTitle",
	__ssrInlineRender: true,
	props: { class: String },
	setup(__props) {
		const props = __props;
		const classes = computed(() => cn("text-2xl font-semibold leading-none tracking-tight", props.class));
		return (_ctx, _push, _parent, _attrs) => {
			_push(`<h3${ssrRenderAttrs(mergeProps({ class: classes.value }, _attrs))}>`);
			ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent);
			_push(`</h3>`);
		};
	}
};
var _sfc_setup = _sfc_main.setup;
_sfc_main.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/CardTitle.vue");
	return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
//#endregion
export { _sfc_main$1 as n, _sfc_main as t };
