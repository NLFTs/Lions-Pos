import { n as cn } from "./Button-Bj0EF1Kv.js";
import { mergeDefaults, mergeProps, unref, useSSRContext } from "vue";
import { ssrRenderAttrs, ssrRenderSlot } from "vue/server-renderer";
//#region src/components/ui/Label.vue
var _sfc_main$1 = {
	__name: "Label",
	__ssrInlineRender: true,
	props: {
		class: String,
		for: String
	},
	setup(__props) {
		return (_ctx, _push, _parent, _attrs) => {
			_push(`<label${ssrRenderAttrs(mergeProps({
				class: unref(cn)("text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70", __props.class),
				for: __props.for
			}, _attrs))}>`);
			ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent);
			_push(`</label>`);
		};
	}
};
var _sfc_setup$1 = _sfc_main$1.setup;
_sfc_main$1.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/Label.vue");
	return _sfc_setup$1 ? _sfc_setup$1(props, ctx) : void 0;
};
//#endregion
//#region src/components/ui/Alert.vue
var _sfc_main = {
	__name: "Alert",
	__ssrInlineRender: true,
	props: /* @__PURE__ */ mergeDefaults({
		variant: String,
		class: String
	}, { variant: "default" }),
	setup(__props) {
		const variants = {
			default: "bg-background text-foreground border",
			destructive: "border-destructive/50 text-destructive dark:border-destructive"
		};
		return (_ctx, _push, _parent, _attrs) => {
			_push(`<div${ssrRenderAttrs(mergeProps({
				role: "alert",
				class: unref(cn)("relative w-full rounded-lg border p-4 [&>svg~*]:pl-7 [&>svg]:absolute [&>svg]:left-4 [&>svg]:top-4", variants[__props.variant], __props.class)
			}, _attrs))}>`);
			ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent);
			_push(`</div>`);
		};
	}
};
var _sfc_setup = _sfc_main.setup;
_sfc_main.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/Alert.vue");
	return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
//#endregion
export { _sfc_main$1 as n, _sfc_main as t };
