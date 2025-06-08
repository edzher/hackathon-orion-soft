'use client'

import * as React from "react"
import { Filters } from "@/components/filters"
import {
    Sidebar,
    SidebarContent,
    SidebarFooter,
    SidebarHeader,
    SidebarMenu,
    SidebarMenuButton,
    SidebarMenuItem,
} from "@/components/ui/sidebar"

import {Button} from "@/components/ui/button";


export function AppSidebar({ ...props }: React.ComponentProps<typeof Sidebar>) {
    return (
        <Sidebar collapsible="offcanvas" {...props}>
            <SidebarHeader>
                <SidebarMenu>
                    <SidebarMenuItem>
                        <SidebarMenuButton
                            asChild
                            className="data-[slot=sidebar-menu-button]:!p-1.5"
                        >
                            <a href="#">
                                <img src="/cdek_logo.png" alt="Logo" className="h-5 w-15" />
                                <span className="text-base font-semibold">PAY TRACK</span>
                            </a>
                        </SidebarMenuButton>
                    </SidebarMenuItem>
                </SidebarMenu>
            </SidebarHeader>
            <SidebarContent>
                <Filters/>
            </SidebarContent>
            <SidebarFooter>
                <Button variant="ghost" asChild size="sm" className="hidden sm:flex">
                    <a
                        href="https://github.com/edzher/hackathon-orion-soft"
                        rel="noopener noreferrer"
                        target="_blank"
                        className="dark:text-foreground"
                    >
                        GitHub
                    </a>
                </Button>
            </SidebarFooter>
        </Sidebar>
    )
}
