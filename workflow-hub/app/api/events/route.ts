import { NextResponse } from "next/server"

export async function GET() {
    const events = [
         {
      id: "e1",
      source: "github",
      type: "push",
      title: "Pushed 3 commits to repo-x",
      body: "Added login fix and tests",
      timestamp: "2025-11-25T10:30:00.000Z",
      metadata: { repo: "repo-x", branch: "main", author: "alice" },
      read: false,
    },
    {
      id: "e2",
      source: "gmail",
      type: "email_received",
      title: "Invoice from ACME",
      body: "Please find attached invoice for Oct.",
      timestamp: "2025-11-24T08:15:00.000Z",
      metadata: { from: "billing@acme.com" },
      read: false,
    }
    ]

return NextResponse.json({events})
}