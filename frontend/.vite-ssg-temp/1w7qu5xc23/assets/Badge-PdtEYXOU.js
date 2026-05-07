import { n as cn } from "./Button-Bj0EF1Kv.js";
import { mergeDefaults, mergeProps, unref, useSSRContext } from "vue";
import { ssrRenderAttrs, ssrRenderSlot } from "vue/server-renderer";
//#region src/components/ui/Badge.vue
var _sfc_main = {
	__name: "Badge",
	__ssrInlineRender: true,
	props: /* @__PURE__ */ mergeDefaults({
		variant: String,
		class: String
	}, { variant: "default" }),
	setup(__props) {
		const variants = {
			default: "bg-primary text-primary-foreground hover:bg-primary/80",
			secondary: "bg-secondary text-secondary-foreground hover:bg-secondary/80",
			destructive: "bg-destructive text-destructive-foreground hover:bg-destructive/80",
			outline: "text-foreground border border-input"
		};
		return (_ctx, _push, _parent, _attrs) => {
			_push(`<div${ssrRenderAttrs(mergeProps({ class: unref(cn)("inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none", variants[__props.variant], __props.class) }, _attrs))}>`);
			ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent);
			_push(`</div>`);
		};
	}
};
var _sfc_setup = _sfc_main.setup;
_sfc_main.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/Badge.vue");
	return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
//#endregion
export { _sfc_main as t };
