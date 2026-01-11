// app/activity/page.tsx
"use client";
import { useEffect, useState } from "react";
import { useQuery, QueryClient, QueryClientProvider } from "@tanstack/react-query";

type Event = {
  id: string;
  source: string;
  type: string;
  title: string;
  body: string;
  timestamp: string;
  metadata: Record<string, any>;
  read: boolean;
};


function EventCard({ e }: { e: Event }) {
 const [val, setVal] = useState<string>(e.read ? "Read" : "Marked");

  const toggleread = () =>{
   setVal((prev) => (prev === "Read" ? "Marked" : "Read"));
}
  return (
    <div style={{ border: "1px solid #ddd", padding: 12, marginBottom: 8, borderRadius: 6 }}>
      <div style={{ fontWeight: 600 }}>
        {e.title} <small style={{ color: "#666" }}>({e.source})</small>
      </div>
      <div style={{ fontSize: 13 }}>{e.body}</div>
      <div style={{ fontSize: 12, color: "#777", marginTop: 6 }}>
        {new Date(e.timestamp).toLocaleString()}
      </div>
      <button className="bg-blue-500 hover:bg-blue-800 p-1 rounded-xl "
      onClick={()=>toggleread()}
      aria-pressed={val === "Read"}>{val}</button>
    </div>
  );
}

function useEvents() {
  return useQuery({
    queryKey: ["events"],
    queryFn: async () => {
      const res = await fetch("/api/events");
      if (!res.ok) throw new Error("Network error");
      const json = await res.json();
    
      // adapt depending on whether API returns { events: [...] } or just [...]
      return json.events ?? json;
    },
    // optional: stale time so we don't refetch too aggressively
    staleTime: 1000 * 60, // 1 minute
  });
}


 export default function ActivityPage() {
  const { data: events = [], isLoading, isError, error } = useEvents();
  const [filter, setfilter] = useState("")
  
  
  

  

  const Filtersearch = events?.filter((ev: Event) => (
    ev.source.toLowerCase().includes(filter.toLowerCase()) ||
    ev.timestamp.toLowerCase().includes(filter.toLowerCase())
  ))
  
  return (
    <main style={{ maxWidth: 800, margin: "32px auto", padding: "0 16px" }}>

      <h1>Activity Feed</h1>

      <input type="text" placeholder="Search" className="border-2" value={filter}
       onChange={(e: React.ChangeEvent<HTMLInputElement>)=>setfilter(e.target.value)} />

       {isLoading ? (
        <p>Loading...</p>
      ) : isError? 
      (
      <p>Error Fetching</p>
      )
      : Filtersearch === undefined ? (
        <p>No events found</p>
      ) : (
        Filtersearch.map((e: Event) => <EventCard key={e.id} e={e} />)
      )}
    </main>
  );
}
