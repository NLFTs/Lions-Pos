import { t as _plugin_vue_export_helper_default } from "./_plugin-vue_export-helper-DMwexRDj.js";
import { defineComponent, mergeProps, onBeforeUnmount, onMounted, ref, unref, useSSRContext, useTemplateRef, watch } from "vue";
import { ssrInterpolate, ssrRenderAttrs, ssrRenderComponent, ssrRenderList, ssrRenderSlot } from "vue/server-renderer";
import { ChevronDown, Zap } from "lucide-vue-next";
import { Mesh, Program, Renderer, Triangle } from "ogl";
//#region src/components/section/landingpage/LandingLayout.vue
var _sfc_main$1 = {
	__name: "LandingLayout",
	__ssrInlineRender: true,
	setup(__props) {
		return (_ctx, _push, _parent, _attrs) => {
			_push(`<div${ssrRenderAttrs(mergeProps({ class: "dark" }, _attrs))} data-v-4363b9fa><div class="min-h-screen bg-zinc-950 flex justify-center text-zinc-100 selection:bg-primary/10 transition-colors duration-300" data-v-4363b9fa><div class="w-full min-h-screen flex flex-col bg-zinc-950 transition-colors duration-300" data-v-4363b9fa>`);
			ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent);
			_push(`</div></div></div>`);
		};
	}
};
var _sfc_setup$2 = _sfc_main$1.setup;
_sfc_main$1.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/section/landingpage/LandingLayout.vue");
	return _sfc_setup$2 ? _sfc_setup$2(props, ctx) : void 0;
};
var LandingLayout_default = /* @__PURE__ */ _plugin_vue_export_helper_default(_sfc_main$1, [["__scopeId", "data-v-4363b9fa"]]);
//#endregion
//#region src/components/section/landingpage/Navbar.vue
var _sfc_main = {
	__name: "Navbar",
	__ssrInlineRender: true,
	props: {
		brandName: {
			type: String,
			default: "obsidian"
		},
		navItems: {
			type: Array,
			required: true,
			default: () => [
				{
					name: "What we offer",
					path: "#offer",
					hasDropdown: true
				},
				{
					name: "Who's it for",
					path: "#target",
					hasDropdown: true
				},
				{
					name: "Pricing",
					path: "#pricing"
				},
				{
					name: "About",
					path: "#about"
				}
			]
		}
	},
	emits: ["navigate"],
	setup(__props) {
		return (_ctx, _push, _parent, _attrs) => {
			_push(`<nav${ssrRenderAttrs(mergeProps({ class: "h-20 flex items-center justify-between px-6 md:px-12 sticky top-0 bg-white/50 dark:bg-zinc-950/50 backdrop-blur-xl z-50 transition-colors duration-300 w-full" }, _attrs))} data-v-7196335e><div class="flex items-center gap-2 cursor-pointer group" data-v-7196335e><div class="h-8 w-8 bg-white dark:bg-zinc-100 rounded-full flex items-center justify-center overflow-hidden border border-zinc-200 dark:border-zinc-800" data-v-7196335e>`);
			ssrRenderSlot(_ctx.$slots, "logo", {}, () => {
				_push(ssrRenderComponent(unref(Zap), { class: "h-4 w-4 text-zinc-950" }, null, _parent));
			}, _push, _parent);
			_push(`</div><span class="text-xl font-medium font-serif tracking-tight text-zinc-900 dark:text-zinc-100" data-v-7196335e>${ssrInterpolate(__props.brandName.toLowerCase())}</span></div><div class="hidden md:flex items-center gap-8" data-v-7196335e><!--[-->`);
			ssrRenderList(__props.navItems, (item) => {
				_push(`<div class="flex items-center gap-1 cursor-pointer group" data-v-7196335e><span class="text-sm font-medium text-zinc-600 dark:text-zinc-400 group-hover:text-zinc-900 dark:group-hover:text-zinc-100 transition-colors" data-v-7196335e>${ssrInterpolate(item.name)}</span>`);
				if (item.hasDropdown) _push(ssrRenderComponent(unref(ChevronDown), { class: "h-3.5 w-3.5 text-zinc-400 group-hover:text-zinc-600 dark:group-hover:text-zinc-200 transition-colors" }, null, _parent));
				else _push(`<!---->`);
				_push(`</div>`);
			});
			_push(`<!--]--></div><div class="flex items-center gap-4" data-v-7196335e>`);
			ssrRenderSlot(_ctx.$slots, "actions", {}, null, _push, _parent);
			_push(`</div></nav>`);
		};
	}
};
var _sfc_setup$1 = _sfc_main.setup;
_sfc_main.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/section/landingpage/Navbar.vue");
	return _sfc_setup$1 ? _sfc_setup$1(props, ctx) : void 0;
};
var Navbar_default = /* @__PURE__ */ _plugin_vue_export_helper_default(_sfc_main, [["__scopeId", "data-v-7196335e"]]);
//#endregion
//#region src/components/ui/background/GradientBlinds.vue?vue&type=script&setup=true&lang.ts
var MAX_COLORS = 8;
var GradientBlinds_vue_vue_type_script_setup_true_lang_default = /* @__PURE__ */ defineComponent({
	__name: "GradientBlinds",
	__ssrInlineRender: true,
	props: {
		className: {},
		dpr: {},
		paused: {
			type: Boolean,
			default: false
		},
		gradientColors: {},
		angle: { default: 0 },
		noise: { default: .3 },
		blindCount: { default: 16 },
		blindMinWidth: { default: 60 },
		mouseDampening: { default: .15 },
		mirrorGradient: {
			type: Boolean,
			default: false
		},
		spotlightRadius: { default: .5 },
		spotlightSoftness: { default: 1 },
		spotlightOpacity: { default: 1 },
		distortAmount: { default: 0 },
		shineDirection: { default: "left" },
		mixBlendMode: { default: "lighten" }
	},
	setup(__props) {
		const hexToRGB = (hex) => {
			const c = hex.replace("#", "").padEnd(6, "0");
			return [
				parseInt(c.slice(0, 2), 16) / 255,
				parseInt(c.slice(2, 4), 16) / 255,
				parseInt(c.slice(4, 6), 16) / 255
			];
		};
		const prepStops = (stops) => {
			const base = (stops && stops.length ? stops : ["#FF9FFC", "#27FF64"]).slice(0, MAX_COLORS);
			if (base.length === 1) base.push(base[0]);
			while (base.length < MAX_COLORS) base.push(base[base.length - 1]);
			const arr = [];
			for (let i = 0; i < MAX_COLORS; i++) arr.push(hexToRGB(base[i]));
			return {
				arr,
				count: Math.max(2, Math.min(MAX_COLORS, stops?.length ?? 2))
			};
		};
		const props = __props;
		const containerRef = useTemplateRef("containerRef");
		const rafRef = ref(0);
		const programRef = ref(null);
		const meshRef = ref(null);
		const geometryRef = ref(null);
		const rendererRef = ref(null);
		const mouseTargetRef = ref([0, 0]);
		const lastTimeRef = ref(0);
		const firstResizeRef = ref(true);
		let cleanup = null;
		const setup = () => {
			const container = containerRef.value;
			if (!container) return;
			const renderer = new Renderer({
				dpr: props.dpr ?? (typeof window !== "undefined" ? window.devicePixelRatio || 1 : 1),
				alpha: true,
				antialias: true
			});
			rendererRef.value = renderer;
			const gl = renderer.gl;
			const canvas = gl.canvas;
			canvas.style.width = "100%";
			canvas.style.height = "100%";
			canvas.style.display = "block";
			container.appendChild(canvas);
			const vertex = `
attribute vec2 position;
attribute vec2 uv;
varying vec2 vUv;

void main() {
  vUv = uv;
  gl_Position = vec4(position, 0.0, 1.0);
}
`;
			const fragment = `
#ifdef GL_ES
precision mediump float;
#endif

uniform vec3  iResolution;
uniform vec2  iMouse;
uniform float iTime;

uniform float uAngle;
uniform float uNoise;
uniform float uBlindCount;
uniform float uSpotlightRadius;
uniform float uSpotlightSoftness;
uniform float uSpotlightOpacity;
uniform float uMirror;
uniform float uDistort;
uniform float uShineFlip;
uniform vec3  uColor0;
uniform vec3  uColor1;
uniform vec3  uColor2;
uniform vec3  uColor3;
uniform vec3  uColor4;
uniform vec3  uColor5;
uniform vec3  uColor6;
uniform vec3  uColor7;
uniform int   uColorCount;

varying vec2 vUv;

float rand(vec2 co){
  return fract(sin(dot(co, vec2(12.9898,78.233))) * 43758.5453);
}

vec2 rotate2D(vec2 p, float a){
  float c = cos(a);
  float s = sin(a);
  return mat2(c, -s, s, c) * p;
}

vec3 getGradientColor(float t){
  float tt = clamp(t, 0.0, 1.0);
  int count = uColorCount;
  if (count < 2) count = 2;
  float scaled = tt * float(count - 1);
  float seg = floor(scaled);
  float f = fract(scaled);

  if (seg < 1.0) return mix(uColor0, uColor1, f);
  if (seg < 2.0 && count > 2) return mix(uColor1, uColor2, f);
  if (seg < 3.0 && count > 3) return mix(uColor2, uColor3, f);
  if (seg < 4.0 && count > 4) return mix(uColor3, uColor4, f);
  if (seg < 5.0 && count > 5) return mix(uColor4, uColor5, f);
  if (seg < 6.0 && count > 6) return mix(uColor5, uColor6, f);
  if (seg < 7.0 && count > 7) return mix(uColor6, uColor7, f);
  if (count > 7) return uColor7;
  if (count > 6) return uColor6;
  if (count > 5) return uColor5;
  if (count > 4) return uColor4;
  if (count > 3) return uColor3;
  if (count > 2) return uColor2;
  return uColor1;
}

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    vec2 uv0 = fragCoord.xy / iResolution.xy;

    float aspect = iResolution.x / iResolution.y;
    vec2 p = uv0 * 2.0 - 1.0;
    p.x *= aspect;
    vec2 pr = rotate2D(p, uAngle);
    pr.x /= aspect;
    vec2 uv = pr * 0.5 + 0.5;

    vec2 uvMod = uv;
    if (uDistort > 0.0) {
      float a = uvMod.y * 6.0;
      float b = uvMod.x * 6.0;
      float w = 0.01 * uDistort;
      uvMod.x += sin(a) * w;
      uvMod.y += cos(b) * w;
    }
    float t = uvMod.x;
    if (uMirror > 0.5) {
      t = 1.0 - abs(1.0 - 2.0 * fract(t));
    }
    vec3 base = getGradientColor(t);

    vec2 offset = vec2(iMouse.x/iResolution.x, iMouse.y/iResolution.y);
  float d = length(uv0 - offset);
  float r = max(uSpotlightRadius, 1e-4);
  float dn = d / r;
  float spot = (1.0 - 2.0 * pow(dn, uSpotlightSoftness)) * uSpotlightOpacity;
  vec3 cir = vec3(spot);
  float stripe = fract(uvMod.x * max(uBlindCount, 1.0));
  if (uShineFlip > 0.5) stripe = 1.0 - stripe;
    vec3 ran = vec3(stripe);

    vec3 col = cir + base - ran;
    col += (rand(gl_FragCoord.xy + iTime) - 0.5) * uNoise;

    fragColor = vec4(col, 1.0);
}

void main() {
    vec4 color;
    mainImage(color, vUv * iResolution.xy);
    gl_FragColor = color;
}
`;
			const { arr: colorArr, count: colorCount } = prepStops(props.gradientColors);
			const uniforms = {
				iResolution: { value: [
					gl.drawingBufferWidth,
					gl.drawingBufferHeight,
					1
				] },
				iMouse: { value: [0, 0] },
				iTime: { value: 0 },
				uAngle: { value: props.angle * Math.PI / 180 },
				uNoise: { value: props.noise },
				uBlindCount: { value: Math.max(1, props.blindCount) },
				uSpotlightRadius: { value: props.spotlightRadius },
				uSpotlightSoftness: { value: props.spotlightSoftness },
				uSpotlightOpacity: { value: props.spotlightOpacity },
				uMirror: { value: props.mirrorGradient ? 1 : 0 },
				uDistort: { value: props.distortAmount },
				uShineFlip: { value: props.shineDirection === "right" ? 1 : 0 },
				uColor0: { value: colorArr[0] },
				uColor1: { value: colorArr[1] },
				uColor2: { value: colorArr[2] },
				uColor3: { value: colorArr[3] },
				uColor4: { value: colorArr[4] },
				uColor5: { value: colorArr[5] },
				uColor6: { value: colorArr[6] },
				uColor7: { value: colorArr[7] },
				uColorCount: { value: colorCount }
			};
			const program = new Program(gl, {
				vertex,
				fragment,
				uniforms
			});
			programRef.value = program;
			const geometry = new Triangle(gl);
			geometryRef.value = geometry;
			meshRef.value = new Mesh(gl, {
				geometry,
				program
			});
			const resize = () => {
				const rect = container.getBoundingClientRect();
				renderer.setSize(rect.width, rect.height);
				uniforms.iResolution.value = [
					gl.drawingBufferWidth,
					gl.drawingBufferHeight,
					1
				];
				if (props.blindMinWidth && props.blindMinWidth > 0) {
					const maxByMinWidth = Math.max(1, Math.floor(rect.width / props.blindMinWidth));
					const effective = props.blindCount ? Math.min(props.blindCount, maxByMinWidth) : maxByMinWidth;
					uniforms.uBlindCount.value = Math.max(1, effective);
				} else uniforms.uBlindCount.value = Math.max(1, props.blindCount);
				if (firstResizeRef.value) {
					firstResizeRef.value = false;
					const cx = gl.drawingBufferWidth / 2;
					const cy = gl.drawingBufferHeight / 2;
					uniforms.iMouse.value = [cx, cy];
					mouseTargetRef.value = [cx, cy];
				}
			};
			resize();
			const ro = new ResizeObserver(resize);
			ro.observe(container);
			const onPointerMove = (e) => {
				const rect = canvas.getBoundingClientRect();
				const scale = renderer.dpr || 1;
				const x = (e.clientX - rect.left) * scale;
				const y = (rect.height - (e.clientY - rect.top)) * scale;
				mouseTargetRef.value = [x, y];
				if (props.mouseDampening <= 0) uniforms.iMouse.value = [x, y];
			};
			canvas.addEventListener("pointermove", onPointerMove);
			const loop = (t) => {
				rafRef.value = requestAnimationFrame(loop);
				uniforms.iTime.value = t * .001;
				if (props.mouseDampening > 0) {
					if (!lastTimeRef.value) lastTimeRef.value = t;
					const dt = (t - lastTimeRef.value) / 1e3;
					lastTimeRef.value = t;
					const tau = Math.max(1e-4, props.mouseDampening);
					let factor = 1 - Math.exp(-dt / tau);
					if (factor > 1) factor = 1;
					const target = mouseTargetRef.value;
					const cur = uniforms.iMouse.value;
					cur[0] += (target[0] - cur[0]) * factor;
					cur[1] += (target[1] - cur[1]) * factor;
				} else lastTimeRef.value = t;
				if (!props.paused && programRef.value && meshRef.value) try {
					renderer.render({ scene: meshRef.value });
				} catch (e) {
					console.error(e);
				}
			};
			rafRef.value = requestAnimationFrame(loop);
			cleanup = () => {
				if (rafRef.value) cancelAnimationFrame(rafRef.value);
				canvas.removeEventListener("pointermove", onPointerMove);
				ro.disconnect();
				if (canvas.parentElement === container) container.removeChild(canvas);
				const callIfFn = (obj, key) => {
					if (obj && typeof obj[key] === "function") obj[key].call(obj);
				};
				callIfFn(programRef.value, "remove");
				callIfFn(geometryRef.value, "remove");
				callIfFn(meshRef.value, "remove");
				callIfFn(rendererRef.value, "destroy");
				programRef.value = null;
				geometryRef.value = null;
				meshRef.value = null;
				rendererRef.value = null;
			};
		};
		onMounted(() => {
			setup();
		});
		onBeforeUnmount(() => {
			cleanup?.();
		});
		watch(props, () => {
			cleanup?.();
			setup();
		}, { deep: true });
		return (_ctx, _push, _parent, _attrs) => {
			_push(`<div${ssrRenderAttrs(mergeProps({
				ref_key: "containerRef",
				ref: containerRef,
				class: ["w-full h-full overflow-hidden relative", __props.className],
				style: { ...__props.mixBlendMode ? { mixBlendMode: __props.mixBlendMode } : {} }
			}, _attrs))}></div>`);
		};
	}
});
//#endregion
//#region src/components/ui/background/GradientBlinds.vue
var _sfc_setup = GradientBlinds_vue_vue_type_script_setup_true_lang_default.setup;
GradientBlinds_vue_vue_type_script_setup_true_lang_default.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/background/GradientBlinds.vue");
	return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
var GradientBlinds_default = GradientBlinds_vue_vue_type_script_setup_true_lang_default;
//#endregion
export { Navbar_default as n, LandingLayout_default as r, GradientBlinds_default as t };
