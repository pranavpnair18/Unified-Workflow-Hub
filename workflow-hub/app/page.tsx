"use client";

import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { fetchNotifications } from "@/lib/FetchNotifications";
import { setNotifications } from "./store/NotificationSlice";
import { selectTotalCount, selectNotifications } from "./store/selectors";
import githublogo from "../public/logos/github-mark.png";
import gmaillogo from "../public/logos/icons8-gmail-100 (1).png";
import calendarlogo from "../public/logos/icons8-calendar.gif";
import customapilogo from "../public/logos/icons8-api-100.png";
import Image from "next/image";
import Link from "next/link";

export default function Home() {
  const dispatch = useDispatch();
  const total = useSelector(selectTotalCount);
  const notifications = useSelector(selectNotifications);
  const [showMessages, setShowMessages] = useState(false);
  const [isOpen, setIsOpen] = useState(false);

  useEffect(() => {
    async function load() {
      const data = await fetchNotifications();
      dispatch(setNotifications(data));
    }
    load();
  }, [dispatch]);

  const counts = [
    { logoUrl: githublogo, count: notifications.github },
    { logoUrl: gmaillogo, count: notifications.gmail },
    { logoUrl: calendarlogo, count: notifications.calendar },
    { logoUrl: customapilogo, count: notifications.customApi },
  ];
  return (
    <div className="p-6 min-h-screen flex flex-col items-center justify-center gap-0 bg-amber-100 text-white">
      {/* Welcome Section */}
      <h1
        className="text-4xl md:text-5xl font-extrabold mb-22 animate-fadeIn mt-5
             bg-linear-to-tr from-amber-200 via-amber-600 to-amber-800 
             bg-clip-text text-transparent p-4 relative tracking-tight"
      >
        Unified Workflow Hub
      </h1>

      {/* Messages Section */}
      <h2
        onClick={() => setShowMessages(!showMessages)}
        className="cursor-pointer top-1 text-xl font-semibold bg-amber-700 px-6 py-3 rounded-lg shadow-lg backdrop-blur-md hover:scale-105 transition-transform duration-300 relative"
      >
        You have {total} new messages ✉️
      </h2>

      {/* Circular/Surrounding Logos with Counts */}
      {showMessages && (
        <div className="grid grid-cols-4 gap-2 top-35 absolute w-xs">
          {counts.map((msg, idx) => (
            <div
              key={idx}
              className="flex flex-col items-center justify-center rounded-full  hover:scale-105 transition-transforma animate-slideIn "
            >
              {/* Logo */}
              <Image
                src={msg.logoUrl}
                alt="connection logo"
                className="w-6 h-6 rounded-full mb-1"
              />
              {/* Count Badge */}
              <span className="text-black font-bold text-sm">{msg.count}</span>
            </div>
          ))}
        </div>
      )}

      {/* Extra Styling / Call-to-Action */}
      <div>
        <p className="mt-6 text-sm opacity-90 animate-slideUp text-amber-700">
          Stay productive, stay connected,everything you need in one place,
        </p>
        <p className="mt-5 text-sm opacity-90 animate-slideUp text-center text-amber-700">
          Everything you need in one place.
        </p>
      </div>

      {/* Example Button */}
      <button className="mt-5 top-2 px-6 py-3 mb-10 bg-white text-indigo-600 font-bold rounded-full shadow-lg hover:scale-105 transition-transform duration-300 animate-pulse">
        <Link href="/dashboard">View Dashboard</Link>
      </button>
    </div>
  );
}
