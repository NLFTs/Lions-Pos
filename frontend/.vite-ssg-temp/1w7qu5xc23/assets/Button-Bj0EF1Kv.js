import { mergeDefaults, mergeProps, useSSRContext } from "vue";
import { ssrRenderAttrs, ssrRenderSlot } from "vue/server-renderer";
import { clsx } from "clsx";
import { twMerge } from "tailwind-merge";
//#region src/lib/utils.js
function cn(...inputs) {
	return twMerge(clsx(inputs));
}
//#endregion
//#region src/components/ui/Button.vue
var _sfc_main = {
	__name: "Button",
	__ssrInlineRender: true,
	props: /* @__PURE__ */ mergeDefaults({
		variant: String,
		size: String,
		class: String,
		disabled: Boolean,
		type: String
	}, {
		variant: "default",
		size: "default",
		disabled: false,
		type: "button"
	}),
	setup(__props) {
		const variants = {
			default: "bg-primary text-primary-foreground hover:bg-primary/90",
			destructive: "bg-destructive text-destructive-foreground hover:bg-destructive/90",
			outline: "border border-input bg-background hover:bg-accent hover:text-accent-foreground",
			secondary: "bg-secondary text-secondary-foreground hover:bg-secondary/80",
			ghost: "hover:bg-accent hover:text-accent-foreground",
			link: "text-primary underline-offset-4 hover:underline"
		};
		const sizes = {
			default: "h-9 px-4 py-2",
			sm: "h-8 px-3 text-xs",
			lg: "h-11 rounded-md px-8",
			icon: "h-9 w-9"
		};
		function getClasses() {
			return cn("inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-md text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50", variants[__props.variant], sizes[__props.size], __props.class);
		}
		return (_ctx, _push, _parent, _attrs) => {
			_push(`<button${ssrRenderAttrs(mergeProps({
				class: getClasses(),
				type: __props.type,
				disabled: __props.disabled
			}, _attrs))}>`);
			ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent);
			_push(`</button>`);
		};
	}
};
var _sfc_setup = _sfc_main.setup;
_sfc_main.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/Button.vue");
	return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
//#endregion
export { cn as n, _sfc_main as t };
