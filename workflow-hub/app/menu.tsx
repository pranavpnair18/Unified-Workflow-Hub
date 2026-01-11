"use client";

import Link from "next/link";
import { useState } from "react";

export default function Menu() {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <>
      {/* Hamburger Button (hidden when menu is open) */}
      {!isOpen && (
        <button
          onClick={() => setIsOpen(true)}
          className="fixed top-6 right-6 z-10000 p-2 rounded-md bg-amber-100 hover:bg-amber-600 transition"
        >
          {/* Hamburger Icon */}
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="h-6 w-6 text-amber-700 hover:text-white"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M4 6h16M4 12h16M4 18h16"
            />
          </svg>
        </button>
      )}

      {/* Sidebar + Backdrop */}
      {isOpen && (
        <>
          {/* Backdrop */}
          <div
            onClick={() => setIsOpen(false)}
            className="fixed inset-0 bg-black/40 backdrop-blur-sm z-9998"
          />

          {/* Sidebar */}
          <div className="fixed top-0 right-0 h-screen w-64 bg-amber-950 text-white shadow-lg z-9999">
            {/* Header */}
            <div className="flex justify-between items-center p-6 border-b border-white/20">
              <span className="font-bold text-xl text-shadow-amber-500">Menu</span>

              <button
                onClick={() => setIsOpen(false)}
                className="p-2 rounded-md hover:bg-amber-800 transition"
              >
                {/* Close Icon */}
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="h-6 w-6 text-white"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M6 18L18 6M6 6l12 12"
                  />
                </svg>
              </button>
            </div>

            {/* Menu Links */}
            <ul className="space-y-4 p-6">
              <li>
                <Link
                  href="/"
                  className="hover:text-amber-200"
                  onClick={() => setIsOpen(false)}
                >
                  Home
                </Link>
              </li>
              <li>
                <Link
                  href="/about"
                  className="hover:text-amber-200"
                  onClick={() => setIsOpen(false)}
                >
                  About
                </Link>
              </li>
              <li>
                <Link
                  href="/integrations"
                  className="hover:text-amber-200"
                  onClick={() => setIsOpen(false)}
                >
                  Connections
                </Link>
              </li>
              <li>
                <Link
                  href="/dashboard"
                  className="hover:text-amber-200"
                  onClick={() => setIsOpen(false)}
                >
                  Messages
                </Link>
              </li>
              <li>
                <Link
                  href="/settings"
                  className="hover:text-amber-200"
                  onClick={() => setIsOpen(false)}
                >
                  Settings
                </Link>
              </li>
            </ul>
          </div>
        </>
      )}
    </>
  );
}
