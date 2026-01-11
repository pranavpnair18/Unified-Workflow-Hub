"use client";

import { useEffect, useState } from "react";
import { PROVIDER_UI } from "@/lib/providers";
import { useDispatch } from "react-redux";
import { setNotifications } from "../store/NotificationSlice";

/* ---------------- TYPES ---------------- */

type ActivityItem = {
  id: string;
  title: string;
  subtitle?: string;
  time: string;
  url?: string;
};

type ActivityCardProps = {
  provider: string;
  items: ActivityItem[];
};

type GitHubEvent = {
  id: number;
  type: string;
  repoName: string;
  actorLogin: string;
  createdAt: string;
  externalUrl?: string;
};

type GmailEvent = {
  id: number;
  fromEmail: string;
  subject: string;
  receivedAt: string;
  externalUrl?: string;
};

type CalendarEvent = {
  id: number;
  title: string;
  startTime: string;
};

type GenericApiEvent = {
  id: number;
  title: string;
  createdAt: string;
};

/* ---------------- HELPERS ---------------- */

function isTodayInIST(dateString: string) {
  const eventDate = new Date(dateString).toLocaleDateString("en-CA", {
    timeZone: "Asia/Kolkata",
  });
  const todayIST = new Date().toLocaleDateString("en-CA", {
    timeZone: "Asia/Kolkata",
  });
  return eventDate === todayIST;
}

function isToday(dateString: string) {
  const d = new Date(dateString);
  const today = new Date();
  return (
    d.getDate() === today.getDate() &&
    d.getMonth() === today.getMonth() &&
    d.getFullYear() === today.getFullYear()
  );
}

/* ---------------- ACTIVITY CARD ---------------- */

function ActivityCard({ provider, items }: ActivityCardProps) {
  const ui = PROVIDER_UI[provider.toLowerCase() as keyof typeof PROVIDER_UI];
  const Icon = ui.icon;

  return (
    <div className="relative rounded-3xl bg-white/70 backdrop-blur-xl border border-white/40 shadow-lg hover:shadow-xl transition overflow-hidden">
      {/* Accent bar */}
      <div className={`absolute top-0 left-0 h-1 w-full ${ui.bg}`} />

      {/* Header */}
      <div className="flex items-center justify-between px-6 py-5">
        <div className="flex items-center gap-3">
          <div className={`p-2 rounded-xl ${ui.bg} bg-opacity-15`}>
            <Icon className={`w-5 h-5 ${ui.text}`} />
          </div>
          <h2 className="text-sm font-semibold text-[#2e1d0b]">
            {ui.label}
          </h2>
        </div>

        <span className="text-xs text-[#7a6230] hover:underline cursor-pointer">
          View all →
        </span>
      </div>

      {/* Body */}
      <div className="px-6 pb-6 space-y-4">
        {items.length === 0 ? (
          <p className="text-sm text-[#9c8b5c] italic">
            No activity today
          </p>
        ) : (
          items.map((item) => (
            <div
              key={item.id}
              className="relative pl-4 border-l-2 border-[#eadfb6] hover:border-[#d4b36a] transition"
            >
              <div className="text-sm font-medium text-[#2e1d0b]">
                {item.title}
              </div>

              {item.subtitle && (
                <div className="text-xs text-[#6f5d2e] mt-0.5">
                  {item.subtitle}
                </div>
              )}

              <div className="flex items-center justify-between mt-1">
                <span className="text-[11px] text-[#9c8b5c]">
                  {item.time}
                </span>

                {item.url && (
                  <a
                    href={item.url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-[11px] text-[#7a4a1a] hover:underline"
                  >
                    Open →
                  </a>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

/* ---------------- DASHBOARD PAGE ---------------- */

export default function DashboardPage() {
  const [githubEvents, setGithubEvents] = useState<GitHubEvent[]>([]);
  const [gmailEvents, setGmailEvents] = useState<GmailEvent[]>([]);
  const [calendarEvents, setCalendarEvents] = useState<CalendarEvent[]>([]);
  const [customApiEvents, setCustomApiEvents] = useState<GenericApiEvent[]>([]);
  const [loading, setLoading] = useState(true);

  const dispatch = useDispatch();

  useEffect(() => {
    async function loadData() {
      try {
        const [ghRes, gmRes, gcRes, capiRes] = await Promise.all([
          fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL}/api/integrations/github/events`),
          fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL}/api/integrations/gmail/messages`),
          fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL}/api/integrations/calendar/events`),
          fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL}/api/integrations/custom_api/events`),
        ]);

        setGithubEvents((await ghRes.json()).filter((e: GitHubEvent) => isToday(e.createdAt)));
        setGmailEvents((await gmRes.json()).filter((e: GmailEvent) => isToday(e.receivedAt)));
        setCalendarEvents((await gcRes.json()).filter((e: CalendarEvent) => isTodayInIST(e.startTime)));
        setCustomApiEvents(await capiRes.json());
      } catch (err) {
        console.error("Dashboard load failed", err);
      } finally {
        setLoading(false);
      }
    }

    loadData();
  }, []);

  const githubActivities = githubEvents.map(e => ({
    id: String(e.id),
    title: e.type,
    subtitle: `repo: ${e.repoName}`,
    time: new Date(e.createdAt).toLocaleTimeString(),
    url: e.externalUrl,
  }));

  const gmailActivities = gmailEvents.map(e => ({
    id: String(e.id),
    title: e.subject || "(No subject)",
    subtitle: `From: ${e.fromEmail}`,
    time: new Date(e.receivedAt).toLocaleTimeString(),
    url: e.externalUrl,
  }));

  const calendarActivities = calendarEvents.map(e => ({
    id: String(e.id),
    title: e.title,
    time: new Date(e.startTime).toLocaleTimeString(),
  }));

  const apiActivities = customApiEvents.map(e => ({
    id: String(e.id),
    title: e.title || "API Event",
    time: new Date(e.createdAt).toLocaleTimeString(),
  }));

  useEffect(() => {
    dispatch(setNotifications({
      github: githubActivities.length,
      gmail: gmailActivities.length,
      calendar: calendarActivities.length,
      customApi: apiActivities.length,
    }));
  }, [dispatch, githubActivities, gmailActivities, calendarActivities, apiActivities]);

  return (
    <div className="min-h-screen bg-linear-to-br from-[#fdfbd4] via-[#f7edd1] to-[#f3e2b3]">
      <div className="max-w-7xl mx-auto px-6 py-10">

        {/* Header */}
        <div className="mb-10">
          <h1 className="text-4xl font-bold text-amber-900 tracking-tight">
            Dashboard
          </h1>
          <p className="text-sm mt-2 text-[#665227]">
            Unified view of today’s activity across your tools
          </p>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-6 mb-12">
          {[
            { label: "GitHub", value: githubActivities.length },
            { label: "Gmail", value: gmailActivities.length },
            { label: "Calendar", value: calendarActivities.length },
            { label: "Custom API", value: apiActivities.length },
          ].map(stat => (
            <div
              key={stat.label}
              className="rounded-3xl bg-white/60 backdrop-blur-xl border border-white/40 p-6 shadow-lg hover:scale-[1.03] transition"
            >
              <div className="text-xs uppercase tracking-wide text-[#725d27]">
                {stat.label}
              </div>
              <div className="text-3xl font-bold text-[#2e1d0b] mt-2">
                {stat.value}
              </div>
            </div>
          ))}
        </div>

        {loading && (
          <p className="text-sm text-[#7a6230] mb-8 animate-pulse">
            Loading today’s activity…
          </p>
        )}

        {/* Activity Cards */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <ActivityCard provider="github" items={githubActivities} />
          <ActivityCard provider="gmail" items={gmailActivities} />
          <ActivityCard provider="calendar" items={calendarActivities} />
          <ActivityCard provider="custom_api" items={apiActivities} />
        </div>

      </div>
    </div>
  );
}
