import { i as useNotification, r as api } from "../main.mjs";
import { t as _sfc_main$1 } from "./Button-Bj0EF1Kv.js";
import { d as usePermission, t as _sfc_main$2 } from "./AppLayout-D1IhsFmL.js";
import { t as _sfc_main$3 } from "./DataTablePagination-CRAPEico.js";
import "./Badge-PdtEYXOU.js";
import { Fragment, Teleport, computed, createBlock, createCommentVNode, createVNode, onMounted, openBlock, ref, renderList, toDisplayString, unref, useSSRContext, vModelText, withCtx, withDirectives } from "vue";
import { ssrInterpolate, ssrRenderAttr, ssrRenderClass, ssrRenderComponent, ssrRenderList, ssrRenderTeleport } from "vue/server-renderer";
import { Check, ChevronDown, ChevronUp, Copy, Database, Download, Loader2, PlayCircle, RefreshCw, Search, X } from "lucide-vue-next";
//#region src/pages/LogsPage.vue
var _sfc_main = {
	__name: "LogsPage",
	__ssrInlineRender: true,
	setup(__props) {
		const { can } = usePermission();
		const { notify } = useNotification();
		const logs = ref([]);
		const pagination = ref({
			page: 0,
			size: 20,
			totalPages: 0,
			totalElements: 0
		});
		const loading = ref(false);
		const error = ref(null);
		const searchQuery = ref("");
		const filteredLogs = computed(() => {
			if (!searchQuery.value) return logs.value;
			const q = searchQuery.value.toLowerCase();
			return logs.value.filter((l) => (l.url || "").toLowerCase().includes(q) || (l.userFullname || "").toLowerCase().includes(q) || (l.method || "").toLowerCase().includes(q));
		});
		const showDetail = ref(false);
		const detailLog = ref(null);
		async function fetchLogs(page = 0) {
			loading.value = true;
			error.value = null;
			try {
				const data = (await api.get(`/api/v1/logs?page=${page}&size=${pagination.value.size}`)).data.data;
				logs.value = data.content;
				pagination.value = {
					page: data.number,
					size: data.size,
					totalPages: data.totalPages,
					totalElements: data.totalElements
				};
			} catch (err) {
				error.value = err.response?.data?.message || "Failed to load logs.";
			} finally {
				loading.value = false;
			}
		}
		function updateLogsSize(newSize) {
			pagination.value.size = newSize;
			fetchLogs(0);
		}
		onMounted(() => fetchLogs());
		function viewDetail(log) {
			detailLog.value = log;
			showDetail.value = true;
		}
		const activeIndex = computed(() => {
			if (!detailLog.value) return -1;
			return filteredLogs.value.findIndex((l) => l.id === detailLog.value.id);
		});
		function nextLog() {
			if (activeIndex.value >= 0 && activeIndex.value < filteredLogs.value.length - 1) detailLog.value = filteredLogs.value[activeIndex.value + 1];
		}
		function prevLog() {
			if (activeIndex.value > 0) detailLog.value = filteredLogs.value[activeIndex.value - 1];
		}
		const isCopied = ref(false);
		async function copyLog() {
			if (!detailLog.value) return;
			const textToCopy = JSON.stringify(detailLog.value, null, 2);
			try {
				if (navigator.clipboard && window.isSecureContext) await navigator.clipboard.writeText(textToCopy);
				else {
					const textArea = document.createElement("textarea");
					textArea.value = textToCopy;
					textArea.style.position = "fixed";
					textArea.style.left = "-999999px";
					textArea.style.top = "-999999px";
					document.body.appendChild(textArea);
					textArea.focus();
					textArea.select();
					try {
						document.execCommand("copy");
					} finally {
						textArea.remove();
					}
				}
				isCopied.value = true;
				notify({
					title: "Log copied",
					message: "Audit log details copied to clipboard.",
					type: "success",
					duration: 3e3
				});
				setTimeout(() => isCopied.value = false, 2e3);
			} catch (err) {
				console.error("Failed to copy log", err);
				notify({
					title: "Copy failed",
					message: "Could not copy log. Clipboard access denied.",
					type: "error",
					duration: 3e3
				});
			}
		}
		function formatDuration(ms) {
			if (ms == null) return "-";
			if (ms < 1e3) return `${ms}ms`;
			return `${(ms / 1e3).toFixed(2)}s`;
		}
		function formatVercelDateTop(dt) {
			if (!dt) return "-";
			const d = new Date(dt);
			return `${d.toLocaleString("en-US", { month: "short" }).toUpperCase()} ${d.getDate().toString().padStart(2, "0")}`;
		}
		function formatVercelDateBottom(dt) {
			if (!dt) return "-";
			const d = new Date(dt);
			return `${d.toLocaleString("en-US", {
				hour12: false,
				hour: "2-digit",
				minute: "2-digit",
				second: "2-digit"
			})}.${d.getMilliseconds().toString().padStart(2, "0").slice(0, 2)} GMT+7`;
		}
		function formatVercelDate(dt) {
			if (!dt) return "-";
			const d = new Date(dt);
			return `${d.toLocaleString("en-US", { month: "short" }).toUpperCase()} ${d.getDate().toString().padStart(2, "0")} ${d.toLocaleString("en-US", {
				hour12: false,
				hour: "2-digit",
				minute: "2-digit",
				second: "2-digit"
			})}.${d.getMilliseconds().toString().padStart(2, "0").slice(0, 2)}`;
		}
		function getStatusTextColor(status) {
			if (!status) return "text-zinc-500";
			const code = parseInt(status);
			if (code >= 200 && code < 300) return "text-emerald-600 dark:text-emerald-400";
			if (code >= 300 && code < 400) return "text-blue-600 dark:text-blue-400";
			if (code >= 400 && code < 500) return "text-amber-600 dark:text-amber-500";
			return "text-red-600 dark:text-red-500";
		}
		return (_ctx, _push, _parent, _attrs) => {
			_push(ssrRenderComponent(_sfc_main$2, _attrs, {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) {
						_push(`<div class="h-[calc(100vh-3rem)] flex flex-col font-sans -m-5 bg-white dark:bg-[#0a0a0a]"${_scopeId}><div class="flex items-center justify-between p-3 lg:px-6 border-b border-zinc-200 dark:border-zinc-800 shrink-0"${_scopeId}><div class="flex items-center gap-2 w-full max-w-2xl bg-zinc-100 dark:bg-[#111111] border border-zinc-200 dark:border-zinc-800 rounded-md px-3 py-1.5 focus-within:ring-1 focus-within:ring-zinc-300 dark:focus-within:ring-zinc-700 transition-shadow"${_scopeId}>`);
						_push(ssrRenderComponent(unref(Search), { class: "h-4 w-4 text-zinc-500 shrink-0" }, null, _parent, _scopeId));
						_push(`<input${ssrRenderAttr("value", searchQuery.value)} placeholder="method:GET" class="bg-transparent border-none outline-none text-[13px] w-full font-mono text-zinc-900 dark:text-zinc-100 placeholder:text-zinc-500 focus:ring-0 px-1"${_scopeId}></div><div class="flex items-center gap-2 ml-4 shrink-0"${_scopeId}>`);
						_push(ssrRenderComponent(_sfc_main$1, {
							variant: "outline",
							size: "sm",
							class: "h-8 gap-2 bg-zinc-100 dark:bg-[#111111] border-zinc-200 dark:border-zinc-800 text-zinc-700 dark:text-zinc-300 hover:bg-zinc-200 dark:hover:bg-[#222]"
						}, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) {
									_push(ssrRenderComponent(unref(PlayCircle), { class: "h-3.5 w-3.5" }, null, _parent, _scopeId));
									_push(`<span class="hidden sm:inline"${_scopeId}>Live</span>`);
								} else return [createVNode(unref(PlayCircle), { class: "h-3.5 w-3.5" }), createVNode("span", { class: "hidden sm:inline" }, "Live")];
							}),
							_: 1
						}, _parent, _scopeId));
						_push(ssrRenderComponent(_sfc_main$1, {
							variant: "outline",
							size: "icon",
							class: "h-8 w-8 bg-zinc-100 dark:bg-[#111111] border-zinc-200 dark:border-zinc-800 text-zinc-700 dark:text-zinc-300 hover:bg-zinc-200 dark:hover:bg-[#222]",
							onClick: ($event) => fetchLogs(0)
						}, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) _push(ssrRenderComponent(unref(RefreshCw), { class: ["h-3.5 w-3.5", { "animate-spin": loading.value }] }, null, _parent, _scopeId));
								else return [createVNode(unref(RefreshCw), { class: ["h-3.5 w-3.5", { "animate-spin": loading.value }] }, null, 8, ["class"])];
							}),
							_: 1
						}, _parent, _scopeId));
						_push(ssrRenderComponent(_sfc_main$1, {
							variant: "outline",
							size: "icon",
							class: "h-8 w-8 bg-zinc-100 dark:bg-[#111111] border-zinc-200 dark:border-zinc-800 text-zinc-700 dark:text-zinc-300 hover:bg-zinc-200 dark:hover:bg-[#222]"
						}, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) _push(ssrRenderComponent(unref(Download), { class: "h-3.5 w-3.5" }, null, _parent, _scopeId));
								else return [createVNode(unref(Download), { class: "h-3.5 w-3.5" })];
							}),
							_: 1
						}, _parent, _scopeId));
						_push(`</div></div><div class="flex-1 overflow-x-auto overflow-y-auto custom-scrollbar relative"${_scopeId}><table class="w-full text-left border-collapse min-w-[800px]"${_scopeId}><thead class="sticky top-0 z-10 bg-white/95 dark:bg-[#0a0a0a]/95 backdrop-blur-sm"${_scopeId}><tr class="text-[11px] font-semibold text-zinc-500 dark:text-zinc-400 border-b border-zinc-200 dark:border-zinc-800"${_scopeId}><th class="px-6 py-2.5 font-medium w-[180px]"${_scopeId}>Time</th><th class="px-4 py-2.5 font-medium w-[120px]"${_scopeId}>Status</th><th class="px-4 py-2.5 font-medium w-[180px]"${_scopeId}>Host</th><th class="px-4 py-2.5 font-medium"${_scopeId}>Request</th><th class="px-4 py-2.5 font-medium"${_scopeId}>Messages</th></tr></thead><tbody class="font-mono text-[14px]"${_scopeId}>`);
						if (loading.value && logs.value.length === 0) {
							_push(`<tr class="border-b border-zinc-100 dark:border-zinc-800/50"${_scopeId}><td colspan="5" class="py-12 text-center"${_scopeId}>`);
							_push(ssrRenderComponent(unref(Loader2), { class: "h-5 w-5 animate-spin text-zinc-500 mx-auto" }, null, _parent, _scopeId));
							_push(`</td></tr>`);
						} else if (filteredLogs.value.length === 0) _push(`<tr class="border-b border-zinc-100 dark:border-zinc-800/50"${_scopeId}><td colspan="5" class="py-12 text-center text-zinc-500 font-sans text-sm"${_scopeId}>No logs found matching your criteria.</td></tr>`);
						else {
							_push(`<!--[-->`);
							ssrRenderList(filteredLogs.value, (log) => {
								_push(`<tr class="border-b border-zinc-100 dark:border-zinc-800/50 odd:bg-white dark:odd:bg-[#0a0a0a] even:bg-zinc-50/80 dark:even:bg-[#111111] hover:!bg-zinc-100 dark:hover:!bg-[#1a1a1a] transition-colors cursor-pointer"${_scopeId}><td class="px-6 py-3 text-zinc-500 dark:text-zinc-400 whitespace-nowrap"${_scopeId}>${ssrInterpolate(formatVercelDate(log.requestAt))}</td><td class="px-4 py-3 whitespace-nowrap font-medium tracking-tight"${_scopeId}><span class="text-zinc-500 dark:text-zinc-400 mr-2.5"${_scopeId}>${ssrInterpolate(log.method || "-")}</span><span class="${ssrRenderClass(getStatusTextColor(log.responseStatus))}"${_scopeId}>${ssrInterpolate(log.responseStatus || "-")}</span></td><td class="px-4 py-3 text-zinc-700 dark:text-zinc-300 truncate max-w-[180px]"${_scopeId}>site-pribadi-2yk...</td><td class="px-4 py-3 text-zinc-700 dark:text-zinc-300"${_scopeId}><div class="flex items-center gap-2 overflow-hidden"${_scopeId}><div class="flex items-center justify-center border border-zinc-200 dark:border-zinc-700 rounded-[3px] bg-white dark:bg-zinc-900 p-0.5 shrink-0"${_scopeId}>`);
								_push(ssrRenderComponent(unref(Database), { class: "h-3.5 w-3.5 text-zinc-500 dark:text-zinc-400" }, null, _parent, _scopeId));
								_push(`</div><span class="truncate"${_scopeId}>${ssrInterpolate(log.url || "-")}</span></div></td><td class="px-4 py-3 text-zinc-500 dark:text-zinc-500 truncate text-[13px] font-sans"${_scopeId}>-</td></tr>`);
							});
							_push(`<!--]-->`);
						}
						_push(`</tbody></table></div><div class="shrink-0 p-2 flex items-center justify-between border-t border-zinc-200 dark:border-zinc-800 bg-white dark:bg-[#0a0a0a]"${_scopeId}><div class="text-[11px] text-zinc-500 px-4 font-mono font-medium"${_scopeId}> Total: ${ssrInterpolate(pagination.value.totalElements)}</div>`);
						if (pagination.value.totalElements > 0 && !loading.value) _push(ssrRenderComponent(_sfc_main$3, {
							page: pagination.value.page + 1,
							"page-size": pagination.value.size,
							total: pagination.value.totalElements,
							"onUpdate:page": ($event) => fetchLogs($event - 1),
							"onUpdate:pageSize": ($event) => updateLogsSize($event)
						}, null, _parent, _scopeId));
						else _push(`<!---->`);
						_push(`</div></div>`);
						ssrRenderTeleport(_push, (_push) => {
							if (showDetail.value && detailLog.value) {
								_push(`<div class="fixed inset-0 z-50 sm:pointer-events-none"${_scopeId}><div class="absolute inset-0 bg-black/60 backdrop-blur-sm sm:pointer-events-auto"${_scopeId}></div><div class="absolute bottom-0 left-0 right-0 sm:left-auto sm:bottom-6 sm:right-6 w-full sm:max-w-[480px] max-h-[85vh] flex flex-col rounded-t-xl sm:rounded-xl bg-white dark:bg-[#0a0a0a] shadow-[0_-8px_30px_rgba(0,0,0,0.12)] sm:shadow-[0_8px_30px_rgba(0,0,0,0.12)] dark:shadow-[0_-8px_40px_rgba(0,0,0,0.5)] sm:dark:shadow-[0_8px_40px_rgba(0,0,0,0.5)] border-t sm:border-y sm:border-x border-zinc-200 dark:border-zinc-800 animate-in slide-in-from-bottom-full sm:slide-in-from-bottom-8 duration-200 sm:pointer-events-auto"${_scopeId}><div class="flex items-center justify-between px-4 py-3 border-b border-zinc-200 dark:border-zinc-800 bg-white dark:bg-[#0a0a0a] rounded-t-xl shrink-0 gap-2"${_scopeId}><div class="flex items-center gap-2 min-w-0 flex-1"${_scopeId}><span class="font-mono text-zinc-900 dark:text-zinc-100 font-bold border border-zinc-200 dark:border-zinc-800 px-1.5 py-0.5 rounded text-xs bg-zinc-100 dark:bg-[#111] shrink-0"${_scopeId}>${ssrInterpolate(detailLog.value.method || "-")}</span><span class="font-mono text-zinc-900 dark:text-zinc-100 text-sm truncate min-w-0"${_scopeId}>${ssrInterpolate(detailLog.value.url || "-")}</span><span class="${ssrRenderClass([getStatusTextColor(detailLog.value.responseStatus), "font-mono text-xs border border-zinc-200 dark:border-zinc-800 px-1.5 py-0.5 rounded bg-zinc-50 dark:bg-emerald-950/20 shrink-0"])}"${_scopeId}>${ssrInterpolate(detailLog.value.responseStatus || "-")}</span></div><div class="flex items-center gap-1 shrink-0"${_scopeId}>`);
								_push(ssrRenderComponent(_sfc_main$1, {
									variant: "ghost",
									size: "icon",
									class: "h-7 w-7 text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100",
									onClick: prevLog,
									disabled: activeIndex.value <= 0
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(ssrRenderComponent(unref(ChevronUp), { class: "h-4 w-4" }, null, _parent, _scopeId));
										else return [createVNode(unref(ChevronUp), { class: "h-4 w-4" })];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$1, {
									variant: "ghost",
									size: "icon",
									class: "h-7 w-7 text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100",
									onClick: nextLog,
									disabled: activeIndex.value === -1 || activeIndex.value >= filteredLogs.value.length - 1
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(ssrRenderComponent(unref(ChevronDown), { class: "h-4 w-4" }, null, _parent, _scopeId));
										else return [createVNode(unref(ChevronDown), { class: "h-4 w-4" })];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`<div class="w-px h-4 bg-zinc-200 dark:bg-zinc-800 mx-1"${_scopeId}></div>`);
								_push(ssrRenderComponent(_sfc_main$1, {
									variant: "ghost",
									size: "icon",
									class: "h-7 w-7 text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100 transition-all",
									onClick: copyLog
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) if (isCopied.value) _push(ssrRenderComponent(unref(Check), { class: "h-4 w-4 text-emerald-500" }, null, _parent, _scopeId));
										else _push(ssrRenderComponent(unref(Copy), { class: "h-4 w-4" }, null, _parent, _scopeId));
										else return [isCopied.value ? (openBlock(), createBlock(unref(Check), {
											key: 0,
											class: "h-4 w-4 text-emerald-500"
										})) : (openBlock(), createBlock(unref(Copy), {
											key: 1,
											class: "h-4 w-4"
										}))];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(ssrRenderComponent(_sfc_main$1, {
									variant: "ghost",
									size: "icon",
									class: "h-7 w-7 text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100",
									onClick: ($event) => showDetail.value = false
								}, {
									default: withCtx((_, _push, _parent, _scopeId) => {
										if (_push) _push(ssrRenderComponent(unref(X), { class: "h-4 w-4" }, null, _parent, _scopeId));
										else return [createVNode(unref(X), { class: "h-4 w-4" })];
									}),
									_: 1
								}, _parent, _scopeId));
								_push(`</div></div><div class="flex-1 overflow-y-auto p-4 custom-scrollbar"${_scopeId}><div class="flex items-start gap-4"${_scopeId}><div class="mt-1 w-3.5 h-3.5 rounded-full border-[2px] border-zinc-400 dark:border-zinc-600 shrink-0"${_scopeId}></div><div class="flex-1 min-w-0"${_scopeId}><div class="flex justify-between items-start mb-3"${_scopeId}><span class="font-semibold text-zinc-900 dark:text-zinc-100"${_scopeId}>Request started</span><div class="text-right text-[11px] font-mono text-zinc-500"${_scopeId}><p${_scopeId}>${ssrInterpolate(formatVercelDateTop(detailLog.value.requestAt))}</p><p${_scopeId}>${ssrInterpolate(formatVercelDateBottom(detailLog.value.requestAt))}</p></div></div><div class="bg-zinc-50 dark:bg-[#111111] border border-zinc-200 dark:border-zinc-800 rounded-lg p-3 space-y-3 font-mono text-[13px] text-zinc-700 dark:text-zinc-300"${_scopeId}><div class="flex items-start"${_scopeId}><span class="w-[110px] shrink-0 text-zinc-500"${_scopeId}>Request ID</span><span class="truncate text-zinc-900 dark:text-zinc-100"${_scopeId}>${ssrInterpolate(detailLog.value.id)}</span></div><div class="h-px w-full bg-zinc-200 dark:bg-zinc-800/60 my-1"${_scopeId}></div><div class="flex items-start"${_scopeId}><span class="w-[110px] shrink-0 text-zinc-500"${_scopeId}>Path</span><span class="truncate text-zinc-900 dark:text-zinc-100"${_scopeId}>${ssrInterpolate(detailLog.value.url)}</span></div><div class="h-px w-full bg-zinc-200 dark:bg-zinc-800/60 my-1"${_scopeId}></div><div class="flex items-start"${_scopeId}><span class="w-[110px] shrink-0 text-zinc-500"${_scopeId}>Host</span><span class="truncate text-zinc-900 dark:text-zinc-100"${_scopeId}>${ssrInterpolate(detailLog.value.userFullname || detailLog.value.userId || "system")}</span></div><div class="h-px w-full bg-zinc-200 dark:bg-zinc-800/60 my-1"${_scopeId}></div><div class="flex items-start"${_scopeId}><span class="w-[110px] shrink-0 text-zinc-500"${_scopeId}>User Agent</span><span class="truncate text-zinc-900 dark:text-zinc-100"${_scopeId}>${ssrInterpolate(detailLog.value.userAgent || "-")}</span></div><div class="h-px w-full bg-zinc-200 dark:bg-zinc-800/60 my-1"${_scopeId}></div><div class="flex items-start"${_scopeId}><span class="w-[110px] shrink-0 text-zinc-500"${_scopeId}>Duration</span><span class="text-zinc-900 dark:text-zinc-100"${_scopeId}>${ssrInterpolate(formatDuration(detailLog.value.durationMs))}</span></div></div></div></div></div></div></div>`);
							} else _push(`<!---->`);
						}, "body", false, _parent);
					} else return [createVNode("div", { class: "h-[calc(100vh-3rem)] flex flex-col font-sans -m-5 bg-white dark:bg-[#0a0a0a]" }, [
						createVNode("div", { class: "flex items-center justify-between p-3 lg:px-6 border-b border-zinc-200 dark:border-zinc-800 shrink-0" }, [createVNode("div", { class: "flex items-center gap-2 w-full max-w-2xl bg-zinc-100 dark:bg-[#111111] border border-zinc-200 dark:border-zinc-800 rounded-md px-3 py-1.5 focus-within:ring-1 focus-within:ring-zinc-300 dark:focus-within:ring-zinc-700 transition-shadow" }, [createVNode(unref(Search), { class: "h-4 w-4 text-zinc-500 shrink-0" }), withDirectives(createVNode("input", {
							"onUpdate:modelValue": ($event) => searchQuery.value = $event,
							placeholder: "method:GET",
							class: "bg-transparent border-none outline-none text-[13px] w-full font-mono text-zinc-900 dark:text-zinc-100 placeholder:text-zinc-500 focus:ring-0 px-1"
						}, null, 8, ["onUpdate:modelValue"]), [[vModelText, searchQuery.value]])]), createVNode("div", { class: "flex items-center gap-2 ml-4 shrink-0" }, [
							createVNode(_sfc_main$1, {
								variant: "outline",
								size: "sm",
								class: "h-8 gap-2 bg-zinc-100 dark:bg-[#111111] border-zinc-200 dark:border-zinc-800 text-zinc-700 dark:text-zinc-300 hover:bg-zinc-200 dark:hover:bg-[#222]"
							}, {
								default: withCtx(() => [createVNode(unref(PlayCircle), { class: "h-3.5 w-3.5" }), createVNode("span", { class: "hidden sm:inline" }, "Live")]),
								_: 1
							}),
							createVNode(_sfc_main$1, {
								variant: "outline",
								size: "icon",
								class: "h-8 w-8 bg-zinc-100 dark:bg-[#111111] border-zinc-200 dark:border-zinc-800 text-zinc-700 dark:text-zinc-300 hover:bg-zinc-200 dark:hover:bg-[#222]",
								onClick: ($event) => fetchLogs(0)
							}, {
								default: withCtx(() => [createVNode(unref(RefreshCw), { class: ["h-3.5 w-3.5", { "animate-spin": loading.value }] }, null, 8, ["class"])]),
								_: 1
							}, 8, ["onClick"]),
							createVNode(_sfc_main$1, {
								variant: "outline",
								size: "icon",
								class: "h-8 w-8 bg-zinc-100 dark:bg-[#111111] border-zinc-200 dark:border-zinc-800 text-zinc-700 dark:text-zinc-300 hover:bg-zinc-200 dark:hover:bg-[#222]"
							}, {
								default: withCtx(() => [createVNode(unref(Download), { class: "h-3.5 w-3.5" })]),
								_: 1
							})
						])]),
						createVNode("div", { class: "flex-1 overflow-x-auto overflow-y-auto custom-scrollbar relative" }, [createVNode("table", { class: "w-full text-left border-collapse min-w-[800px]" }, [createVNode("thead", { class: "sticky top-0 z-10 bg-white/95 dark:bg-[#0a0a0a]/95 backdrop-blur-sm" }, [createVNode("tr", { class: "text-[11px] font-semibold text-zinc-500 dark:text-zinc-400 border-b border-zinc-200 dark:border-zinc-800" }, [
							createVNode("th", { class: "px-6 py-2.5 font-medium w-[180px]" }, "Time"),
							createVNode("th", { class: "px-4 py-2.5 font-medium w-[120px]" }, "Status"),
							createVNode("th", { class: "px-4 py-2.5 font-medium w-[180px]" }, "Host"),
							createVNode("th", { class: "px-4 py-2.5 font-medium" }, "Request"),
							createVNode("th", { class: "px-4 py-2.5 font-medium" }, "Messages")
						])]), createVNode("tbody", { class: "font-mono text-[14px]" }, [loading.value && logs.value.length === 0 ? (openBlock(), createBlock("tr", {
							key: 0,
							class: "border-b border-zinc-100 dark:border-zinc-800/50"
						}, [createVNode("td", {
							colspan: "5",
							class: "py-12 text-center"
						}, [createVNode(unref(Loader2), { class: "h-5 w-5 animate-spin text-zinc-500 mx-auto" })])])) : filteredLogs.value.length === 0 ? (openBlock(), createBlock("tr", {
							key: 1,
							class: "border-b border-zinc-100 dark:border-zinc-800/50"
						}, [createVNode("td", {
							colspan: "5",
							class: "py-12 text-center text-zinc-500 font-sans text-sm"
						}, "No logs found matching your criteria.")])) : (openBlock(true), createBlock(Fragment, { key: 2 }, renderList(filteredLogs.value, (log) => {
							return openBlock(), createBlock("tr", {
								key: log.id,
								class: "border-b border-zinc-100 dark:border-zinc-800/50 odd:bg-white dark:odd:bg-[#0a0a0a] even:bg-zinc-50/80 dark:even:bg-[#111111] hover:!bg-zinc-100 dark:hover:!bg-[#1a1a1a] transition-colors cursor-pointer",
								onClick: ($event) => viewDetail(log)
							}, [
								createVNode("td", { class: "px-6 py-3 text-zinc-500 dark:text-zinc-400 whitespace-nowrap" }, toDisplayString(formatVercelDate(log.requestAt)), 1),
								createVNode("td", { class: "px-4 py-3 whitespace-nowrap font-medium tracking-tight" }, [createVNode("span", { class: "text-zinc-500 dark:text-zinc-400 mr-2.5" }, toDisplayString(log.method || "-"), 1), createVNode("span", { class: getStatusTextColor(log.responseStatus) }, toDisplayString(log.responseStatus || "-"), 3)]),
								createVNode("td", { class: "px-4 py-3 text-zinc-700 dark:text-zinc-300 truncate max-w-[180px]" }, "site-pribadi-2yk..."),
								createVNode("td", { class: "px-4 py-3 text-zinc-700 dark:text-zinc-300" }, [createVNode("div", { class: "flex items-center gap-2 overflow-hidden" }, [createVNode("div", { class: "flex items-center justify-center border border-zinc-200 dark:border-zinc-700 rounded-[3px] bg-white dark:bg-zinc-900 p-0.5 shrink-0" }, [createVNode(unref(Database), { class: "h-3.5 w-3.5 text-zinc-500 dark:text-zinc-400" })]), createVNode("span", { class: "truncate" }, toDisplayString(log.url || "-"), 1)])]),
								createVNode("td", { class: "px-4 py-3 text-zinc-500 dark:text-zinc-500 truncate text-[13px] font-sans" }, "-")
							], 8, ["onClick"]);
						}), 128))])])]),
						createVNode("div", { class: "shrink-0 p-2 flex items-center justify-between border-t border-zinc-200 dark:border-zinc-800 bg-white dark:bg-[#0a0a0a]" }, [createVNode("div", { class: "text-[11px] text-zinc-500 px-4 font-mono font-medium" }, " Total: " + toDisplayString(pagination.value.totalElements), 1), pagination.value.totalElements > 0 && !loading.value ? (openBlock(), createBlock(_sfc_main$3, {
							key: 0,
							page: pagination.value.page + 1,
							"page-size": pagination.value.size,
							total: pagination.value.totalElements,
							"onUpdate:page": ($event) => fetchLogs($event - 1),
							"onUpdate:pageSize": ($event) => updateLogsSize($event)
						}, null, 8, [
							"page",
							"page-size",
							"total",
							"onUpdate:page",
							"onUpdate:pageSize"
						])) : createCommentVNode("", true)])
					]), (openBlock(), createBlock(Teleport, { to: "body" }, [showDetail.value && detailLog.value ? (openBlock(), createBlock("div", {
						key: 0,
						class: "fixed inset-0 z-50 sm:pointer-events-none"
					}, [createVNode("div", {
						class: "absolute inset-0 bg-black/60 backdrop-blur-sm sm:pointer-events-auto",
						onClick: ($event) => showDetail.value = false
					}, null, 8, ["onClick"]), createVNode("div", { class: "absolute bottom-0 left-0 right-0 sm:left-auto sm:bottom-6 sm:right-6 w-full sm:max-w-[480px] max-h-[85vh] flex flex-col rounded-t-xl sm:rounded-xl bg-white dark:bg-[#0a0a0a] shadow-[0_-8px_30px_rgba(0,0,0,0.12)] sm:shadow-[0_8px_30px_rgba(0,0,0,0.12)] dark:shadow-[0_-8px_40px_rgba(0,0,0,0.5)] sm:dark:shadow-[0_8px_40px_rgba(0,0,0,0.5)] border-t sm:border-y sm:border-x border-zinc-200 dark:border-zinc-800 animate-in slide-in-from-bottom-full sm:slide-in-from-bottom-8 duration-200 sm:pointer-events-auto" }, [createVNode("div", { class: "flex items-center justify-between px-4 py-3 border-b border-zinc-200 dark:border-zinc-800 bg-white dark:bg-[#0a0a0a] rounded-t-xl shrink-0 gap-2" }, [createVNode("div", { class: "flex items-center gap-2 min-w-0 flex-1" }, [
						createVNode("span", { class: "font-mono text-zinc-900 dark:text-zinc-100 font-bold border border-zinc-200 dark:border-zinc-800 px-1.5 py-0.5 rounded text-xs bg-zinc-100 dark:bg-[#111] shrink-0" }, toDisplayString(detailLog.value.method || "-"), 1),
						createVNode("span", { class: "font-mono text-zinc-900 dark:text-zinc-100 text-sm truncate min-w-0" }, toDisplayString(detailLog.value.url || "-"), 1),
						createVNode("span", { class: ["font-mono text-xs border border-zinc-200 dark:border-zinc-800 px-1.5 py-0.5 rounded bg-zinc-50 dark:bg-emerald-950/20 shrink-0", getStatusTextColor(detailLog.value.responseStatus)] }, toDisplayString(detailLog.value.responseStatus || "-"), 3)
					]), createVNode("div", { class: "flex items-center gap-1 shrink-0" }, [
						createVNode(_sfc_main$1, {
							variant: "ghost",
							size: "icon",
							class: "h-7 w-7 text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100",
							onClick: prevLog,
							disabled: activeIndex.value <= 0
						}, {
							default: withCtx(() => [createVNode(unref(ChevronUp), { class: "h-4 w-4" })]),
							_: 1
						}, 8, ["disabled"]),
						createVNode(_sfc_main$1, {
							variant: "ghost",
							size: "icon",
							class: "h-7 w-7 text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100",
							onClick: nextLog,
							disabled: activeIndex.value === -1 || activeIndex.value >= filteredLogs.value.length - 1
						}, {
							default: withCtx(() => [createVNode(unref(ChevronDown), { class: "h-4 w-4" })]),
							_: 1
						}, 8, ["disabled"]),
						createVNode("div", { class: "w-px h-4 bg-zinc-200 dark:bg-zinc-800 mx-1" }),
						createVNode(_sfc_main$1, {
							variant: "ghost",
							size: "icon",
							class: "h-7 w-7 text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100 transition-all",
							onClick: copyLog
						}, {
							default: withCtx(() => [isCopied.value ? (openBlock(), createBlock(unref(Check), {
								key: 0,
								class: "h-4 w-4 text-emerald-500"
							})) : (openBlock(), createBlock(unref(Copy), {
								key: 1,
								class: "h-4 w-4"
							}))]),
							_: 1
						}),
						createVNode(_sfc_main$1, {
							variant: "ghost",
							size: "icon",
							class: "h-7 w-7 text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100",
							onClick: ($event) => showDetail.value = false
						}, {
							default: withCtx(() => [createVNode(unref(X), { class: "h-4 w-4" })]),
							_: 1
						}, 8, ["onClick"])
					])]), createVNode("div", { class: "flex-1 overflow-y-auto p-4 custom-scrollbar" }, [createVNode("div", { class: "flex items-start gap-4" }, [createVNode("div", { class: "mt-1 w-3.5 h-3.5 rounded-full border-[2px] border-zinc-400 dark:border-zinc-600 shrink-0" }), createVNode("div", { class: "flex-1 min-w-0" }, [createVNode("div", { class: "flex justify-between items-start mb-3" }, [createVNode("span", { class: "font-semibold text-zinc-900 dark:text-zinc-100" }, "Request started"), createVNode("div", { class: "text-right text-[11px] font-mono text-zinc-500" }, [createVNode("p", null, toDisplayString(formatVercelDateTop(detailLog.value.requestAt)), 1), createVNode("p", null, toDisplayString(formatVercelDateBottom(detailLog.value.requestAt)), 1)])]), createVNode("div", { class: "bg-zinc-50 dark:bg-[#111111] border border-zinc-200 dark:border-zinc-800 rounded-lg p-3 space-y-3 font-mono text-[13px] text-zinc-700 dark:text-zinc-300" }, [
						createVNode("div", { class: "flex items-start" }, [createVNode("span", { class: "w-[110px] shrink-0 text-zinc-500" }, "Request ID"), createVNode("span", { class: "truncate text-zinc-900 dark:text-zinc-100" }, toDisplayString(detailLog.value.id), 1)]),
						createVNode("div", { class: "h-px w-full bg-zinc-200 dark:bg-zinc-800/60 my-1" }),
						createVNode("div", { class: "flex items-start" }, [createVNode("span", { class: "w-[110px] shrink-0 text-zinc-500" }, "Path"), createVNode("span", { class: "truncate text-zinc-900 dark:text-zinc-100" }, toDisplayString(detailLog.value.url), 1)]),
						createVNode("div", { class: "h-px w-full bg-zinc-200 dark:bg-zinc-800/60 my-1" }),
						createVNode("div", { class: "flex items-start" }, [createVNode("span", { class: "w-[110px] shrink-0 text-zinc-500" }, "Host"), createVNode("span", { class: "truncate text-zinc-900 dark:text-zinc-100" }, toDisplayString(detailLog.value.userFullname || detailLog.value.userId || "system"), 1)]),
						createVNode("div", { class: "h-px w-full bg-zinc-200 dark:bg-zinc-800/60 my-1" }),
						createVNode("div", { class: "flex items-start" }, [createVNode("span", { class: "w-[110px] shrink-0 text-zinc-500" }, "User Agent"), createVNode("span", { class: "truncate text-zinc-900 dark:text-zinc-100" }, toDisplayString(detailLog.value.userAgent || "-"), 1)]),
						createVNode("div", { class: "h-px w-full bg-zinc-200 dark:bg-zinc-800/60 my-1" }),
						createVNode("div", { class: "flex items-start" }, [createVNode("span", { class: "w-[110px] shrink-0 text-zinc-500" }, "Duration"), createVNode("span", { class: "text-zinc-900 dark:text-zinc-100" }, toDisplayString(formatDuration(detailLog.value.durationMs)), 1)])
					])])])])])])) : createCommentVNode("", true)]))];
				}),
				_: 1
			}, _parent));
		};
	}
};
var _sfc_setup = _sfc_main.setup;
_sfc_main.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/pages/LogsPage.vue");
	return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
//#endregion
export { _sfc_main as default };
