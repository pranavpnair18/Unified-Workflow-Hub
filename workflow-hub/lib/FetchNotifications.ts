// lib/fetchNotifications.ts
export async function fetchNotifications() {
  const [ghRes, gmRes, gcRes, capiRes] = await Promise.all([
    fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL}/api/integrations/github/events`),
    fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL}/api/integrations/gmail/messages`),
    fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL}/api/integrations/calendar/events`),
    fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL}/api/integrations/custom_api/events`)
  ]);

  const ghData = await ghRes.json();
  const gmData = await gmRes.json();
  const gcData = await gcRes.json();
  const capiData = await capiRes.json();

  return {
    github: ghData.filter((e: any) => isToday(e.createdAt)).length,
    gmail: gmData.filter((e: any) => isToday(e.receivedAt)).length,
    calendar: gcData.filter((e: any) => isTodayInIST(e.startTime)).length,
    customApi: capiData.length,
  };
}

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

