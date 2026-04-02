// SSE live console handler
document.addEventListener('DOMContentLoaded', () => {
    const consoleEl = document.getElementById('liveConsole');
    const statusEl  = document.getElementById('runStatus');
    const reportBtn = document.getElementById('reportBtn');
    const runId     = consoleEl?.dataset.runId;

    if (!consoleEl || !runId) return;

    const source = new EventSource('/api/run/' + runId + '/stream');

    source.onmessage = (event) => {
        const line = event.data;

        if (line === '[HAG_COMPLETE]') {
            source.close();
            if (statusEl) {
                statusEl.textContent = 'COMPLETE';
                statusEl.className = 'badge badge-pass';
            }
            if (reportBtn) reportBtn.style.display = 'inline-flex';
            return;
        }

        const span = document.createElement('div');
        span.textContent = line;

        // Color-code based on content
        if (line.includes('[ERROR]') || line.includes('FAIL')) {
            span.className = 'line-error';
        } else if (line.includes('[PASS]') || line.includes('PASSED')) {
            span.className = 'line-pass';
        } else if (line.includes('[WARN]')) {
            span.className = 'line-warn';
        } else if (line.includes('[INFO]')) {
            span.className = 'line-info';
        }

        consoleEl.appendChild(span);
        consoleEl.scrollTop = consoleEl.scrollHeight;
    };

    source.onerror = () => {
        source.close();
        const span = document.createElement('div');
        span.textContent = '── stream ended ──';
        span.className = 'line-warn';
        consoleEl.appendChild(span);
    };
});
