import {
  Github,
  Mail,
  Calendar,
  Plug
} from "lucide-react";

export const PROVIDER_UI = {
  github: {
    label: "GitHub",
    icon: Github,
    bg: "bg-purple-50",
    text: "text-purple-700",
    badge: "bg-purple-100 text-purple-700"
  },
  gmail: {
    label: "Gmail",
    icon: Mail,
    bg: "bg-red-50",
    text: "text-red-700",
    badge: "bg-red-100 text-red-700"
  },
  calendar: {
    label: "Calendar",
    icon: Calendar,
    bg: "bg-green-50",
    text: "text-green-700",
    badge: "bg-green-100 text-green-700"
  },
   // ✅ ADD THIS
  custom_api: {
    label: "Custom API",
    icon: Plug,            // or Server
    text: "text-purple-600",
    bg: "bg-purple-50"
  },
} as const;
