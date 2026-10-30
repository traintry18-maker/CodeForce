package com.example.data

import com.example.data.model.CodeFileEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.SupportedLanguage

data class ProjectTemplate(
    val title: String,
    val description: String,
    val language: SupportedLanguage,
    val files: List<Pair<String, String>> // (fileName, content)
)

object ProjectTemplates {
    val templates: List<ProjectTemplate> = listOf(
        ProjectTemplate(
            title = "React Web Showcase",
            description = "Interactive React UI with State, Effect, and live visual preview",
            language = SupportedLanguage.REACT,
            files = listOf(
                "App.jsx" to """// React 18+ Interactive Component
import React, { useState, useEffect } from 'react';

export default function App() {
  const [count, setCount] = useState(0);
  const [items, setItems] = useState([
    { id: 1, name: 'Cloud Sync Engine', active: true },
    { id: 2, name: 'Real-time Syntax Parser', active: true },
    { id: 3, name: 'Vercel Deployment Pipeline', active: false }
  ]);

  const toggleItem = (id) => {
    setItems(items.map(it => it.id === id ? { ...it, active: !it.active } : it));
  };

  return (
    <div style={{ padding: '24px', fontFamily: 'system-ui, sans-serif', background: '#0D1117', color: '#E6EDF3', minHeight: '100vh' }}>
      <header style={{ borderBottom: '1px solid #30363D', paddingBottom: '16px' }}>
        <h1 style={{ color: '#58A6FF', margin: 0 }}>⚡ CodeForge React Studio</h1>
        <p style={{ color: '#8B949E' }}>Building cloud-ready frontends at lightning speed</p>
      </header>

      <main style={{ marginTop: '20px' }}>
        <div style={{ background: '#161B22', padding: '16px', borderRadius: '8px', border: '1px solid #30363D' }}>
          <h2>Counter: <span style={{ color: '#7EE787' }}>{count}</span></h2>
          <button
            onClick={() => setCount(c => c + 1)}
            style={{ padding: '8px 16px', background: '#238636', color: '#FFF', border: 'none', borderRadius: '6px', cursor: 'pointer', marginRight: '8px' }}>
            Increment
          </button>
          <button
            onClick={() => setCount(0)}
            style={{ padding: '8px 16px', background: '#DA3633', color: '#FFF', border: 'none', borderRadius: '6px', cursor: 'pointer' }}>
            Reset
          </button>
        </div>

        <h3 style={{ marginTop: '24px' }}>Deployment Modules</h3>
        <ul style={{ listStyle: 'none', padding: 0 }}>
          {items.map(item => (
            <li
              key={item.id}
              onClick={() => toggleItem(item.id)}
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                padding: '12px',
                background: '#161B22',
                marginBottom: '8px',
                borderRadius: '6px',
                border: '1px solid #30363D',
                cursor: 'pointer'
              }}>
              <span>{item.name}</span>
              <span style={{ color: item.active ? '#3FB950' : '#F85149' }}>
                {item.active ? '● LIVE' : '○ PAUSED'}
              </span>
            </li>
          ))}
        </ul>
      </main>
    </div>
  );
}
""",
                "index.html" to """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>React Showcase - CodeForge</title>
  <style>
    body { margin: 0; background: #0D1117; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
  </style>
</head>
<body>
  <div id="root"></div>
</body>
</html>
""",
                "styles.css" to """/* CodeForge Custom Theme Stylesheet */
:root {
  --primary-accent: #58A6FF;
  --bg-dark: #0D1117;
  --card-bg: #161B22;
  --border-color: #30363D;
  --success-color: #3FB950;
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  background-color: var(--bg-dark);
  color: #c9d1d9;
}
"""
            )
        ),
        ProjectTemplate(
            title = "Python Data & Analytics",
            description = "Python algorithms, statistical processing, and model evaluation",
            language = SupportedLanguage.PYTHON,
            files = listOf(
                "main.py" to """# CodeForge Python Execution Environment
import math
import time

class MetricsAnalyzer:
    def __init__(self, dataset_name: str):
        self.name = dataset_name
        self.samples = []

    def load_metrics(self, data: list):
        self.samples = data
        print(f"Loaded {len(data)} sample telemetry points for [{self.name}]")

    def compute_summary(self) -> dict:
        if not self.samples:
            return {"error": "Empty dataset"}
        total = sum(self.samples)
        avg = total / len(self.samples)
        variance = sum((x - avg) ** 2 for x in self.samples) / len(self.samples)
        std_dev = math.sqrt(variance)
        return {
            "dataset": self.name,
            "count": len(self.samples),
            "sum": round(total, 2),
            "mean": round(avg, 2),
            "std_dev": round(std_dev, 2),
            "min": min(self.samples),
            "max": max(self.samples)
        }

def run_benchmark():
    analyzer = MetricsAnalyzer("CloudLatencyMonitor")
    analyzer.load_metrics([12.4, 15.2, 11.9, 45.1, 14.8, 13.0, 16.5, 9.8, 22.3])
    
    stats = analyzer.compute_summary()
    print("=== SUMMARY METRICS ===")
    for key, val in stats.items():
        print(f"{key:>10}: {val}")

if __name__ == "__main__":
    start_time = time.time()
    run_benchmark()
    print(f"Executed in {(time.time() - start_time)*1000:.2f}ms")
"""
            )
        ),
        ProjectTemplate(
            title = "Kotlin Modern Backend",
            description = "Kotlin coroutines, Flow streaming, and sealed state transitions",
            language = SupportedLanguage.KOTLIN,
            files = listOf(
                "Main.kt" to """package com.codeforge.cloud

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

sealed interface DeploymentEvent {
    object Idle : DeploymentEvent
    data class Building(val progress: Int) : DeploymentEvent
    data class Completed(val url: String, val latencyMs: Long) : DeploymentEvent
    data class Failed(val reason: String) : DeploymentEvent
}

class DeploymentEngine(private val appName: String) {
    fun deployStream(): Flow<DeploymentEvent> = flow {
        emit(DeploymentEvent.Idle)
        delay(200)
        for (p in 20..100 step 20) {
            emit(DeploymentEvent.Building(p))
            delay(150)
        }
        val generatedUrl = "https://${'$'}{appName.lowercase()}.codeforge.cloud"
        emit(DeploymentEvent.Completed(generatedUrl, 42L))
    }
}

fun main() = runBlocking {
    println("🚀 Starting CodeForge Deployment Pipeline...")
    val engine = DeploymentEngine("omni-builder-api")
    
    engine.deployStream().collect { event ->
        when (event) {
            is DeploymentEvent.Idle -> println("[STATUS] Idle: Preparing container image...")
            is DeploymentEvent.Building -> println("[BUILD] Progress: ${'$'}{event.progress}% complete")
            is DeploymentEvent.Completed -> println("✅ [SUCCESS] Live at: ${'$'}{event.url} (latency: ${'$'}{event.latencyMs}ms)")
            is DeploymentEvent.Failed -> println("❌ [ERROR] Deployment failed: ${'$'}{event.reason}")
        }
    }
}
"""
            )
        ),
        ProjectTemplate(
            title = "TypeScript REST & Types",
            description = "TypeScript interfaces, generics, and asynchronous server routes",
            language = SupportedLanguage.TYPESCRIPT,
            files = listOf(
                "app.ts" to """// TypeScript Cloud Service Architecture
interface ServiceConfig {
  readonly serviceName: string;
  readonly port: number;
  readonly environment: 'dev' | 'staging' | 'production';
  readonly enableLogging: boolean;
}

interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
  timestamp: string;
}

class CloudRouter {
  private readonly routes = new Map<string, (req: any) => Promise<any>>();

  public register<T>(path: string, handler: (req: any) => Promise<T>): void {
    this.routes.set(path, handler);
    console.log(`[Router] Registered route -> ${'$'}{path}`);
  }

  public async dispatch<T>(path: string, payload: any): Promise<ApiResponse<T>> {
    const handler = this.routes.get(path);
    if (!handler) {
      return {
        success: false,
        error: `Endpoint '${'$'}{path}' not found`,
        timestamp: new Date().toISOString()
      };
    }
    const result = await handler(payload);
    return {
      success: true,
      data: result,
      timestamp: new Date().toISOString()
    };
  }
}

// Initialize Router
const router = new CloudRouter();
router.register('/v1/deploy', async (body) => ({
  status: 'DEPLOYED',
  target: 'Vercel-Edge-Region-1',
  deployedAt: Date.now()
}));

async function main() {
  const res = await router.dispatch('/v1/deploy', { app: 'my-ts-app' });
  console.log('Result:', JSON.stringify(res, null, 2));
}

main();
"""
            )
        ),
        ProjectTemplate(
            title = "Swift iOS Architecture",
            description = "SwiftUI state management, View composition, and Swift concurrency",
            language = SupportedLanguage.SWIFT,
            files = listOf(
                "ContentView.swift" to """import SwiftUI

struct CloudProject: Identifiable, Hashable {
    let id = UUID()
    let name: String
    let language: String
    var isLive: Bool
}

struct ContentView: View {
    @State private var projects = [
        CloudProject(name: "Quantum Engine", language: "Swift", isLive: true),
        CloudProject(name: "Microservice Gateway", language: "Kotlin", isLive: true),
        CloudProject(name: "Edge Cache CDN", language: "TypeScript", isLive: false)
    ]
    @State private var newProjectTitle: String = ""

    var body: some View {
        NavigationStack {
            List {
                Section("Active Workspaces") {
                    ForEach(projects) { project in
                        HStack {
                            VStack(alignment: .leading, spacing: 4) {
                                Text(project.name)
                                    .font(.headline)
                                Text(project.language)
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            }
                            Spacer()
                            Circle()
                                .fill(project.isLive ? Color.green : Color.orange)
                                .frame(width: 10, height: 10)
                        }
                    }
                }
            }
            .navigationTitle("CodeForge Cloud")
        }
    }
}
"""
            )
        ),
        ProjectTemplate(
            title = "Dart & Flutter Mobile",
            description = "Dart language OOP, Streams, and Flutter widget architecture",
            language = SupportedLanguage.DART,
            files = listOf(
                "main.dart" to """// CodeForge Dart & Flutter Blueprint
class CodeBuilder {
  final String projectId;
  final String runtime;
  final List<String> buildSteps = [];

  CodeBuilder({required this.projectId, required this.runtime});

  Stream<String> runBuildPipeline() async* {
    yield '📦 Resolving dependencies for ${'$'}runtime...';
    await Future.delayed(const Duration(milliseconds: 300));
    yield '⚙️ Compiling Ahead-of-Time (AOT)...';
    await Future.delayed(const Duration(milliseconds: 400));
    yield '🚀 Optimizing WebAssembly bundle...';
    await Future.delayed(const Duration(milliseconds: 200));
    yield '✨ Successfully deployed to edge network!';
  }
}

void main() async {
  print('Starting Dart Runner...');
  final builder = CodeBuilder(projectId: 'dart-app-01', runtime: 'Flutter Web');
  await for (final status in builder.runBuildPipeline()) {
    print(status);
  }
}
"""
            )
        ),
        ProjectTemplate(
            title = "SQL Database Schema",
            description = "SQL tables, constraints, foreign keys, indexes, and analytics queries",
            language = SupportedLanguage.SQL,
            files = listOf(
                "schema.sql" to """-- CodeForge Cloud Database Schema
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE projects (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    language VARCHAR(30) NOT NULL,
    cloud_status VARCHAR(20) DEFAULT 'IDLE',
    deployed_url TEXT,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_projects_user_id ON projects(user_id);
CREATE INDEX idx_projects_status ON projects(cloud_status);

-- Analytical Query: Most Active Languages
SELECT 
    language,
    COUNT(*) AS total_projects,
    COUNT(CASE WHEN cloud_status = 'DEPLOYED' THEN 1 END) AS deployed_count,
    ROUND(100.0 * COUNT(CASE WHEN cloud_status = 'DEPLOYED' THEN 1 END) / COUNT(*), 1) AS deploy_rate_pct
FROM projects
GROUP BY language
ORDER BY total_projects DESC;
"""
            )
        ),
        ProjectTemplate(
            title = "C++ High Performance",
            description = "Modern C++20 memory, algorithms, vectors, and chrono timers",
            language = SupportedLanguage.CPP,
            files = listOf(
                "main.cpp" to """#include <iostream>
#include <vector>
#include <numeric>
#include <chrono>
#include <string>

template <typename T>
class MatrixBuffer {
private:
    std::vector<T> data;
    size_t rows;
    size_t cols;

public:
    MatrixBuffer(size_t r, size_t c) : rows(r), cols(c), data(r * c, 0) {}

    void set(size_t r, size_t c, T val) {
        data[r * cols + c] = val;
    }

    T get(size_t r, size_t c) const {
        return data[r * cols + c];
    }

    T sum() const {
        return std::accumulate(data.begin(), data.end(), T(0));
    }
};

int main() {
    std::cout << "🚀 CodeForge C++20 Compute Engine\n";
    auto start = std::chrono::high_resolution_clock::now();

    MatrixBuffer<double> matrix(100, 100);
    for (size_t i = 0; i < 100; ++i) {
        for (size_t j = 0; j < 100; ++j) {
            matrix.set(i, j, static_cast<double>(i * j));
        }
    }

    double total = matrix.sum();
    auto end = std::chrono::high_resolution_clock::now();
    std::chrono::duration<double, std::milli> elapsed = end - start;

    std::cout << "Computed Matrix Sum: " << total << "\n";
    std::cout << "Time elapsed: " << elapsed.count() << " ms\n";
    return 0;
}
"""
            )
        ),
        ProjectTemplate(
            title = "C Systems Program",
            description = "Pure C pointers, structures, memory allocation, and algorithms",
            language = SupportedLanguage.C,
            files = listOf(
                "main.c" to """#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct Node {
    int data;
    struct Node* next;
} Node;

Node* create_node(int value) {
    Node* node = (Node*)malloc(sizeof(Node));
    if (node != NULL) {
        node->data = value;
        node->next = NULL;
    }
    return node;
}

void print_list(Node* head) {
    Node* curr = head;
    printf("List: [");
    while (curr != NULL) {
        printf(" %d", curr->data);
        curr = curr->next;
    }
    printf(" ]\n");
}

int main() {
    printf("CodeForge C Runtime Engine\n");
    Node* head = create_node(10);
    head->next = create_node(20);
    head->next->next = create_node(30);

    print_list(head);

    // Free memory
    Node* tmp;
    while (head != NULL) {
        tmp = head;
        head = head->next;
        free(tmp);
    }
    printf("Memory cleanly deallocated.\n");
    return 0;
}
"""
            )
        ),
        ProjectTemplate(
            title = "Java Enterprise Service",
            description = "Java 17+ records, Streams API, and clean service layers",
            language = SupportedLanguage.JAVA,
            files = listOf(
                "Main.java" to """package com.codeforge;

import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public record DeploymentTarget(String name, String region, boolean active) {}

    public static void main(String[] args) {
        System.out.println("⚡ CodeForge Java 17+ Cloud Runtime");

        List<DeploymentTarget> targets = List.of(
            new DeploymentTarget("us-east-vercel", "us-east-1", true),
            new DeploymentTarget("eu-central-render", "eu-central-1", true),
            new DeploymentTarget("ap-tokyo-aws", "ap-northeast-1", false)
        );

        List<String> activeRegions = targets.stream()
            .filter(DeploymentTarget::active)
            .map(DeploymentTarget::region)
            .sorted()
            .collect(Collectors.toList());

        System.out.println("Active Global Regions: " + activeRegions);
    }
}
"""
            )
        ),
        ProjectTemplate(
            title = "C# .NET Service",
            description = "C# modern records, LINQ queries, and task concurrency",
            language = SupportedLanguage.CSHARP,
            files = listOf(
                "Program.cs" to """using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

public record CloudBuild(string Id, string Language, int DurationSec);

class Program {
    static async Task Main(string[] args) {
        Console.WriteLine("🌐 CodeForge C# .NET Cloud Runner");

        var builds = new List<CloudBuild> {
            new("bld-101", "C#", 14),
            new("bld-102", "React", 28),
            new("bld-103", "Python", 9),
            new("bld-104", "Kotlin", 35)
        };

        var fastBuilds = builds.Where(b => b.DurationSec < 30)
                               .OrderBy(b => b.DurationSec);

        Console.WriteLine("High-speed builds (< 30s):");
        foreach (var b in fastBuilds) {
            Console.WriteLine($" -> {b.Id}: {b.Language} completed in {b.DurationSec}s");
        }

        await Task.Delay(100);
        Console.WriteLine("All tasks completed.");
    }
}
"""
            )
        ),
        ProjectTemplate(
            title = "PHP API Engine",
            description = "PHP JSON REST router with auth simulation and query parameters",
            language = SupportedLanguage.PHP,
            files = listOf(
                "index.php" to """<?php
header('Content-Type: application/json; charset=utf-8');

class CloudDeployer {
    private string ${'$'}appName;

    public function __construct(string ${'$'}appName) {
        ${'$'}this->appName = ${'$'}appName;
    }

    public function generatePayload(): array {
        return [
            'status' => 'success',
            'app' => ${'$'}this->appName,
            'engine' => 'PHP 8.2',
            'timestamp' => date('c'),
            'endpoints' => [
                'GET /health' => 'ok',
                'POST /deploy' => 'triggered'
            ]
        ];
    }
}

${'$'}deployer = new CloudDeployer("CodeForge-PHP-Service");
echo json_encode(${'$'}deployer->generatePayload(), JSON_PRETTY_PRINT);
"""
            )
        ),
        ProjectTemplate(
            title = "Ruby Backend Script",
            description = "Ruby elegant object-oriented architecture and block enumerators",
            language = SupportedLanguage.RUBY,
            files = listOf(
                "app.rb" to """# CodeForge Ruby Cloud Script
class Workspace
  attr_reader :name, :files

  def initialize(name)
    @name = name
    @files = []
  end

  def add_file(filename, size_kb)
    @files << { name: filename, size: size_kb }
  end

  def total_size
    @files.sum { |f| f[:size] }
  end
end

ws = Workspace.new("Omni-App")
ws.add_file("app.rb", 4.2)
ws.add_file("index.html", 12.8)
ws.add_file("styles.css", 8.5)

puts "=== Ruby Workspace #{ws.name} ==="
puts "Total files: #{ws.files.count}"
puts "Total footprint: #{ws.total_size.round(2)} KB"
"""
            )
        ),
        ProjectTemplate(
            title = "Lua Game & Scripting",
            description = "Lua table manipulations, game loop logic, and coroutines",
            language = SupportedLanguage.LUA,
            files = listOf(
                "script.lua" to """-- CodeForge Lua Environment
local Entity = {}
Entity.__index = Entity

function Entity.new(id, x, y)
    local self = setmetatable({}, Entity)
    self.id = id
    self.x = x
    self.y = y
    self.hp = 100
    return self
end

function Entity:move(dx, dy)
    self.x = self.x + dx
    self.y = self.y + dy
    print(string.format("Entity %s moved to (%d, %d)", self.id, self.x, self.y))
end

local player = Entity.new("Player-1", 10, 20)
player:move(5, -2)
print("Entity HP: " .. player.hp)
"""
            )
        ),
        ProjectTemplate(
            title = "React Native Mobile",
            description = "React Native mobile components with Flexbox styling",
            language = SupportedLanguage.REACT_NATIVE,
            files = listOf(
                "App.native.jsx" to """import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView } from 'react-native';

export default function NativeApp() {
  const [synced, setSynced] = useState(true);

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>CodeForge Native</Text>
        <Text style={styles.subtitle}>Cross-Device Sync Studio</Text>
      </View>

      <TouchableOpacity 
        style={styles.card} 
        onPress={() => setSynced(!synced)}>
        <Text style={styles.cardTitle}>Cloud Sync Status</Text>
        <Text style={[styles.badge, synced ? styles.green : styles.orange]}>
          {synced ? 'SYNCED TO ALL DEVICES' : 'PENDING SYNC'}
        </Text>
      </TouchableOpacity>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#0F172A', padding: 20 },
  header: { marginBottom: 24, marginTop: 40 },
  title: { fontSize: 26, fontWeight: 'bold', color: '#38BDF8' },
  subtitle: { fontSize: 14, color: '#94A3B8' },
  card: { backgroundColor: '#1E293B', padding: 20, borderRadius: 12 },
  cardTitle: { color: '#F1F5F9', fontSize: 18, fontWeight: '600' },
  badge: { marginTop: 10, fontWeight: 'bold' },
  green: { color: '#4ADE80' },
  orange: { color: '#FB923C' }
});
"""
            )
        ),
        ProjectTemplate(
            title = "Terminal Automation Shell",
            description = "Bash / Shell deployment script with error handling and pipes",
            language = SupportedLanguage.TERMINAL,
            files = listOf(
                "deploy.sh" to """#!/bin/bash
set -e

echo "=========================================="
echo "⚡ CodeForge Cloud Deployment Automation"
echo "=========================================="

APP_NAME="codeforge-service"
ENV="production"
TIMESTAMP=${'$'}(date +"%Y-%m-%d %T")

echo "[INFO] Starting build for '${'$'}APP_NAME' in '${'$'}ENV' at ${'$'}TIMESTAMP"

# Step 1: Health check
echo "[1/4] Running pre-flight environment checks..."
sleep 1
echo " -> OK: Docker daemon and Node.js v20 runtime verified"

# Step 2: Build bundle
echo "[2/4] Bundling multi-language assets..."
sleep 1
echo " -> OK: 14 modules compiled (0 errors, 0 warnings)"

# Step 3: Deploy to Edge
echo "[3/4] Uploading artifacts to Vercel/Cloudflare Edge CDN..."
sleep 1
echo " -> OK: Edge nodes populated in 24 regions"

# Step 4: Verification
echo "[4/4] Conducting SSL handshake and HTTP 200 verification..."
echo "✅ DEPLOYMENT COMPLETE! Application is live at: https://${'$'}APP_NAME.codeforge.cloud"
"""
            )
        )
    )
}
