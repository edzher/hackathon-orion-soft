'use client'

import { AppSidebar } from "@/components/app-sidebar"
import { ChartAreaInteractive } from "@/components/chart"
import { Statistics } from "@/components/statistics"
import { AppHeader } from "@/components/app-header"
import {
    SidebarInset,
    SidebarProvider,
} from "@/components/ui/sidebar"

import "./globals.css"
import React from "react";
import {VacanciesTable} from "@/components/table";

export default function Home() {


    return (
      <SidebarProvider
          style={
              {
                  "--sidebar-width": "calc(var(--spacing) * 72)",
                  "--header-height": "calc(var(--spacing) * 12)",
              } as React.CSSProperties
          }
      >
          <AppSidebar variant="inset"/>
          <SidebarInset>
              <AppHeader />
              <div className="flex flex-1 flex-col">
                  <div className="@container/main flex flex-1 flex-col gap-2">
                      <div className="flex flex-col gap-4 py-4 md:gap-6 md:py-6">
                          <Statistics/>
                          <div className="px-4 lg:px-6">
                              <ChartAreaInteractive/>
                          </div>
                          <VacanciesTable/>
                      </div>
                </div>
              </div>
          </SidebarInset>
      </SidebarProvider>
    )
}
