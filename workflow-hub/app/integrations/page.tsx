"use client";

import { useSearchParams } from "next/navigation";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

type Integration = {
  id: number;
  provider: string;
  connectionName: string;
  type: string;
  status: "ACTIVE" | "DISCONNECTED";
  lastSyncedAt?: string;
  capabilities: string[];
};

export default function IntegrationsPage() {
  const router = useRouter();
  const searchParams = useSearchParams();

  /* ---------------- PROVIDERS ---------------- */

  const providers = [
    { id: "calendar", name: "Calendar", oauth: true },
    { id: "gmail", name: "Gmail", oauth: true },
    { id: "github", name: "GitHub", oauth: true },
    { id: "slack", name: "Slack", oauth: true },
    { id: "jira", name: "Atlassian Jira", oauth: true },
    { id: "custom_api", name: "Custom API (API Key)", oauth: false },
  ];

  /* ---------------- STATE ---------------- */

  const [integrations, setIntegrations] = useState<Integration[]>([]);
  const [loadingIntegrations, setLoadingIntegrations] = useState(true);

  const [selected, setSelected] = useState<string | null>(null);
  const [connectionName, setConnectionName] = useState("");
  const [apiKey, setApiKey] = useState("");
  const [status, setStatus] = useState<string | null>(null);

  const [endpointUrl, setEndpointUrl] = useState("");
  const [authHeaderName, setAuthHeaderName] = useState("Authorization");
  const [pollInterval, setPollInterval] = useState(15);

  const [connected, setConnected] = useState<string | null>(null);
  const [showMessage, setShowMessage] = useState(false);

  /* ---------------- EFFECTS ---------------- */

  useEffect(() => {
    loadIntegrations();
  }, []);

  useEffect(() => {
    setConnected(searchParams.get("connected"));
  }, [searchParams]);

  useEffect(() => {
    if (!connected) return;

    setShowMessage(true);
    const t = setTimeout(() => {
      setShowMessage(false);
      router.replace("/integrations");
    }, 3000);

    return () => clearTimeout(t);
  }, [connected]);

  /* ---------------- API ---------------- */

  async function loadIntegrations() {
    setLoadingIntegrations(true);
    try {
      const res = await fetch(
        `${process.env.NEXT_PUBLIC_BACKEND_URL}/api/integrations`
      );
      const json = await res.json();
      setIntegrations(Array.isArray(json) ? json : json.data || []);
    } catch {
      setIntegrations([]);
    } finally {
      setLoadingIntegrations(false);
    }
  }

  async function disconnectIntegration(id: number) {
    await fetch(
      `${process.env.NEXT_PUBLIC_BACKEND_URL}/api/integrations/${id}`,
      { method: "DELETE" }
    );
    await loadIntegrations();
  }

  async function handleConnect(providerId: string) {
    const provider = providers.find((p) => p.id === providerId);
    if (!provider) return;

    if (provider.oauth) {
      window.location.href =
        `${process.env.NEXT_PUBLIC_BACKEND_URL}/api/integrations/oauth/start` +
        `?provider=${providerId}&connectionName=${encodeURIComponent(
          connectionName || providerId
        )}`;
      return;
    }

    try {
      await fetch(
        `${process.env.NEXT_PUBLIC_BACKEND_URL}/api/integrations/${providerId}/connect`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            connectionName: connectionName || providerId,
            apiKey,
            endpointUrl,
            authHeaderName,
            pollInterval,
          }),
        }
      );

      await loadIntegrations();
      setSelected(null);
    } catch (err: any) {
      setStatus(err.message);
    }
  }

  /* ---------------- UI ---------------- */

  return (
    <div className="min-h-screen bg-amber-100">
      <div className="max-w-6xl mx-auto px-6 py-10">

        {/* Header */}
        <div className="mb-10">
          <h1 className="text-4xl font-bold text-[#2e1d0b] tracking-tight">
            Connections
          </h1>
          <p className="text-sm mt-2 text-[#7a6230] max-w-xl">
            Connect third-party services so Workflow Hub can sync data
            automatically.
          </p>
        </div>

        {/* Success Message */}
        {showMessage && connected && (
          <div className="mb-6 rounded-xl bg-green-50 border border-green-200 p-4 text-sm text-green-700">
            ✅ {connected} connected successfully
          </div>
        )}

        {/* Providers */}
        <div className="grid grid-cols-1 gap-6">
          {providers.map((p) => {
            const integration = integrations.find(
              (i) =>
                i.provider === p.id ||
                (p.id === "calendar" && i.provider === "google") ||
                (p.id === "gmail" && i.provider === "google")
            );

            return (
              <div
  key={p.id}
  className="rounded-3xl bg-white/70 backdrop-blur-xl 
             border border-white/40 p-6 shadow-lg hover:shadow-xl transition"
>
  <div className="grid grid-cols-1 md:grid-cols-[1.2fr_2fr_1.2fr] gap-6 items-center">
    
    {/* LEFT: Provider Info */}
    <div>
      <h2 className="text-xl font-semibold text-[#2e1d0b]">
        {p.name}
      </h2>

      {integration?.lastSyncedAt && (
        <p className="text-xs text-[#7a6230] mt-1">
          Last synced{" "}
          {new Date(integration.lastSyncedAt).toLocaleString()}
        </p>
      )}
    </div>

    {/* MIDDLE: Capabilities */}
    <div className="flex flex-wrap gap-2 justify-start md:justify-center">
      {integration?.capabilities?.length ? (
        integration.capabilities.map((cap) => (
          <span
            key={cap}
            className="px-3 py-1 text-xs rounded-full
                       bg-linear-to-br from-[#f6ebc8] to-[#eddcaa]
                       text-[#5c4a1d] border border-[#e5d39b]"
          >
            {cap.replace("_", " ")}
          </span>
        ))
      ) : (
        <span className="text-xs text-[#9c8b5c] italic">
          No capabilities
        </span>
      )}
    </div>

    {/* RIGHT: Status + Actions */}
    <div className="flex items-center justify-start md:justify-end gap-3 flex-wrap">
      {integration?.status === "ACTIVE" && (
        <span className="px-3 py-1 text-xs rounded-full bg-green-100 text-green-700">
          Active
        </span>
      )}

      {integration?.status === "DISCONNECTED" && (
        <span className="px-3 py-1 text-xs rounded-full bg-red-100 text-red-600">
          Disconnected
        </span>
      )}

      {!integration && (
        <button
          onClick={() => setSelected(p.id)}
          className="px-4 py-2 rounded-lg bg-blue-600 text-white text-sm hover:bg-blue-800"
        >
          Connect
        </button>
      )}

      {integration?.status === "ACTIVE" && (
        <button
          onClick={() => disconnectIntegration(integration.id)}
          className="px-4 py-2 rounded-lg bg-red-300 text-red-600 text-sm hover:bg-red-200"
        >
          Disconnect
        </button>
      )}

      {integration?.status === "DISCONNECTED" && (
        <button
          onClick={() => setSelected(p.id)}
          className="px-4 py-2 rounded-lg bg-blue-500 text-white text-sm hover:bg-blue-700"
        >
          Reconnect
        </button>
      )}
    </div>
  </div>
</div>
            );
          })}
        </div>

        {/* CONFIG MODAL */}
        {selected && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-6">
            <div
              className="absolute inset-0 bg-black/40 backdrop-blur-sm"
              onClick={() => setSelected(null)}
            />
            <div className="relative bg-white rounded-3xl shadow-xl max-w-lg w-full p-6 z-10">
              <h3 className="text-xl font-semibold mb-4 text-[#2e1d0b]">
                Configure {selected}
              </h3>

              {selected === "custom_api" && (
                <div className="space-y-4 mb-4">
                  <input
                    placeholder="Endpoint URL"
                    value={endpointUrl}
                    onChange={(e) => setEndpointUrl(e.target.value)}
                    className="w-full border rounded-lg p-2"
                  />
                  <input
                    placeholder="Auth Header Name"
                    value={authHeaderName}
                    onChange={(e) =>
                      setAuthHeaderName(e.target.value)
                    }
                    className="w-full border rounded-lg p-2"
                  />
                  <input
                    type="number"
                    min={1}
                    value={pollInterval}
                    onChange={(e) =>
                      setPollInterval(Number(e.target.value))
                    }
                    className="w-full border rounded-lg p-2"
                  />
                </div>
              )}

              <input
                placeholder="Connection name"
                value={connectionName}
                onChange={(e) => setConnectionName(e.target.value)}
                className="w-full border rounded-lg p-2 mb-4"
              />

              {!providers.find((p) => p.id === selected)?.oauth && (
                <input
                  placeholder="API Key"
                  value={apiKey}
                  onChange={(e) => setApiKey(e.target.value)}
                  className="w-full border rounded-lg p-2 mb-4"
                />
              )}

              <div className="flex gap-3">
                <button
                  onClick={() => handleConnect(selected)}
                  className="px-4 py-2 rounded-lg bg-[#7a4a1a] text-white"
                >
                  Connect
                </button>
                <button
                  onClick={() => setSelected(null)}
                  className="px-4 py-2 rounded-lg border"
                >
                  Cancel
                </button>
              </div>

              {status && (
                <p className="mt-3 text-sm text-red-600">
                  {status}
                </p>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
