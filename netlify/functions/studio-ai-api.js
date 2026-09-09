const OPENAI_URL = "https://api.openai.com/v1/responses";

function json(data, status = 200) {
  return {
    statusCode: status,
    headers: {
      "Content-Type": "application/json; charset=utf-8",
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Headers": "Content-Type",
      "Access-Control-Allow-Methods": "POST, OPTIONS"
    },
    body: JSON.stringify(data)
  };
}

function outputText(data) {
  if (typeof data?.output_text === "string") return data.output_text.trim();
  let text = "";
  for (const item of data?.output || []) {
    for (const part of item?.content || []) {
      if (part?.type === "output_text" && typeof part.text === "string") text += part.text;
    }
  }
  return text.trim();
}

async function callOpenAI(payload, key) {
  const response = await fetch(OPENAI_URL, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${key}`,
      "Content-Type": "application/json"
    },
    body: JSON.stringify(payload)
  });
  const data = await response.json();
  if (!response.ok) throw new Error(data?.error?.message || `OpenAI HTTP ${response.status}`);
  return data;
}

function cleanJson(text) {
  return text.trim().replace(/^```json\s*/i, "").replace(/^```\s*/i, "").replace(/\s*```$/i, "");
}

exports.handler = async (event) => {
  if (event.httpMethod === "OPTIONS") return json({ ok: true });
  if (event.httpMethod !== "POST") return json({ ok: false, error: "POST only" }, 405);

  const key = process.env.OPENAI_API_KEY;
  if (!key) return json({ ok: false, error: "OPENAI_API_KEY is not configured" }, 500);

  try {
    const body = JSON.parse(event.body || "{}");

    if (body.action === "analyze_scene") {
      const images = Array.isArray(body.images) ? body.images.slice(0, 3) : [];
      const content = [{
        type: "input_text",
        text:
          "Analyze these ordered frames as one scene for a professional video-editing application. " +
          "Use only visible evidence. Do not identify real people. Return JSON only. " +
          `Project: ${String(body.project || "")}. Style: ${String(body.style || "")}. ` +
          `Scene time: ${Number(body.startMs || 0)}-${Number(body.endMs || 0)} ms. ` +
          "Return exactly these fields: summary string, shotType string, subjects string array, actions string array, " +
          "environment string, visibleText string array, mood string, importance integer 0-100, openingPotential integer 0-100, " +
          "climaxPotential integer 0-100, transitionPotential integer 0-100, editingNotes string array, confidence integer 0-100."
      }];

      for (const image of images) {
        if (typeof image === "string" && image.startsWith("data:image/")) {
          content.push({ type: "input_image", image_url: image, detail: "low" });
        }
      }
      if (content.length === 1) return json({ ok: false, error: "No valid images" }, 400);

      const response = await callOpenAI({
        model: "gpt-5.4-mini",
        instructions: "You are Studio AI's visual scene-understanding engine. Analyze only what is visible and return valid JSON only.",
        input: [{ role: "user", content }]
      }, key);

      const raw = outputText(response);
      if (!raw) throw new Error("Vision model returned an empty response");
      const scene = JSON.parse(cleanJson(raw));
      return json({ ok: true, scene });
    }

    const message = String(body.message || "").trim();
    if (!message) return json({ ok: false, error: "message is required" }, 400);

    const context = {
      project: String(body.project || ""),
      style: String(body.style || ""),
      memory: body.memory || {},
      video: body.video || {}
    };

    const response = await callOpenAI({
      model: "gpt-5.4-mini",
      instructions: "You are Studio AI, a professional video-editing assistant. Never claim to have seen visual content unless images were supplied.",
      input: [{ role: "user", content: [{ type: "input_text", text: `PROJECT CONTEXT:\n${JSON.stringify(context)}\n\nUSER MESSAGE:\n${message}` }] }]
    }, key);

    const reply = outputText(response);
    if (!reply) throw new Error("OpenAI returned an empty response");
    return json({ ok: true, reply });
  } catch (error) {
    return json({ ok: false, error: error?.message || "Server error" }, 500);
  }
};
