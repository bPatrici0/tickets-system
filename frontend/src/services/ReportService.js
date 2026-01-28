import { jsPDF } from "jspdf";
import autoTable from "jspdf-autotable";

class ReportService {
    async generateSystemReport(stats, tickets) {
        try {
            const doc = new jsPDF();
            const timestamp = new Date().toLocaleString();

            const safeStats = {
                totalUsers: stats.totalUsers || 0,
                totalAdmins: stats.totalAdmins || 0,
                totalRegularUsers: stats.totalRegularUsers || 0,
                activeUsers: stats.activeUsers || 0,
                inactiveUsers: stats.inactiveUsers || 0,
                openTickets: stats.openTickets || 0,
                inProgressTickets: stats.inProgressTickets || 0,
                resolvedTickets: stats.resolvedTickets || 0,
                totalTickets: stats.totalTickets || 0
            };

            // Cabecera Matrix
            doc.setFillColor(0, 0, 0);
            doc.rect(0, 0, 210, 40, 'F');

            doc.setTextColor(0, 255, 65);
            doc.setFontSize(22);
            doc.text("> SISTEMA DE TICKETS - REPORTE DE ESTADO", 10, 20);

            doc.setFontSize(10);
            doc.setTextColor(0, 150, 0);
            doc.text(`Fecha de generación: ${timestamp}`, 10, 30);
            const systemState = safeStats.openTickets > 10 ? 'ESTADO CRÍTICO' : 'ESTADO ESTABLE';
            doc.text(`Diagnóstico de Red: [${systemState}]`, 10, 35);

            // KPIs - Sección de Rendimiento (Tabular)
            const successRate = safeStats.totalTickets > 0
                ? ((safeStats.resolvedTickets / safeStats.totalTickets) * 100).toFixed(1)
                : "0.0";

            doc.setTextColor(0, 0, 0);
            doc.setFontSize(16);
            doc.text("1. Análisis de Rendimiento (KPIs)", 10, 55);

            autoTable(doc, {
                startY: 60,
                head: [["Métrica", "Valor", "Estado"]],
                body: [
                    ["Tasa de Resolución", `${successRate}%`, parseFloat(successRate) > 70 ? "ÓPTIMO" : "MEJORABLE"],
                    ["Carga del Sistema", `${((safeStats.totalTickets - safeStats.resolvedTickets) / (safeStats.totalTickets || 1) * 100).toFixed(1)}%`, safeStats.openTickets > 5 ? "ALTA" : "NORMAL"],
                    ["Eficiencia de Cierre", `${((safeStats.resolvedTickets / (safeStats.totalTickets || 1)) * 100).toFixed(1)}%`, "SISTEMA ACTUANDO"]
                ],
                theme: 'grid',
                headStyles: { fillColor: [0, 80, 0] }
            });

            // 2. Usuarios
            doc.setFontSize(16);
            doc.text("2. Distribución de Población (Usuarios)", 10, doc.lastAutoTable.finalY + 15);

            autoTable(doc, {
                startY: doc.lastAutoTable.finalY + 20,
                head: [["Categoría", "Cantidad"]],
                body: [
                    ["Total de Usuarios", safeStats.totalUsers],
                    ["Administradores", safeStats.totalAdmins],
                    ["Usuarios Estándar", safeStats.totalRegularUsers],
                    ["Usuarios ACTIVOS", safeStats.activeUsers],
                    ["Usuarios INACTIVOS", safeStats.inactiveUsers]
                ],
                theme: 'striped',
                headStyles: { fillColor: [0, 100, 0] }
            });

            // 3. Tickets por Estado
            doc.setFontSize(16);
            doc.text("3. Análisis de Incidencias (Tickets)", 10, doc.lastAutoTable.finalY + 15);

            const total = safeStats.totalTickets || 1;
            autoTable(doc, {
                startY: doc.lastAutoTable.finalY + 20,
                head: [["Estado", "Conteo", "Porcentaje"]],
                body: [
                    ["ABIERTO", safeStats.openTickets, `${(safeStats.openTickets / total * 100).toFixed(1)}%`],
                    ["EN PROGRESO", safeStats.inProgressTickets, `${(safeStats.inProgressTickets / total * 100).toFixed(1)}%`],
                    ["RESUELTO", safeStats.resolvedTickets, `${(safeStats.resolvedTickets / total * 100).toFixed(1)}%`],
                    ["TOTAL", safeStats.totalTickets, "100%"]
                ],
                theme: 'grid',
                headStyles: { fillColor: [0, 120, 0] }
            });

            // 4. Prioridad
            const priorityCount = {
                'BAJA': tickets ? tickets.filter(t => t.prioridad === 'BAJA').length : 0,
                'MEDIA': tickets ? tickets.filter(t => t.prioridad === 'MEDIA').length : 0,
                'ALTA': tickets ? tickets.filter(t => t.prioridad === 'ALTA').length : 0,
                'CRITICA': tickets ? tickets.filter(t => t.prioridad === 'CRITICA').length : 0
            };

            doc.setFontSize(16);
            doc.text("4. Distribución por Prioridad", 10, doc.lastAutoTable.finalY + 15);

            autoTable(doc, {
                startY: doc.lastAutoTable.finalY + 20,
                head: [["Prioridad", "Frecuencia", "Nivel de Urgencia"]],
                body: [
                    ["CRÍTICA", priorityCount.CRITICA, priorityCount.CRITICA > 0 ? "!!! ALERTA ROJA !!!" : "NINGUNA"],
                    ["ALTA", priorityCount.ALTA, "ELEVADA"],
                    ["MEDIA", priorityCount.MEDIA, "NORMAL"],
                    ["BAJA", priorityCount.BAJA, "MÍNIMA"]
                ],
                theme: 'striped',
                headStyles: { fillColor: [50, 50, 50] }
            });

            // 5. Alertas Críticas (Solo si hay)
            const criticalOnes = tickets ? tickets.filter(t => t.prioridad === 'CRITICA') : [];
            if (criticalOnes.length > 0) {
                doc.addPage();
                doc.setTextColor(200, 0, 0);
                doc.setFontSize(18);
                doc.text("ALERTA: TICKETS CRÍTICOS DETECTADOS", 10, 20);

                autoTable(doc, {
                    startY: 30,
                    head: [["ID", "Título", "Estado"]],
                    body: criticalOnes.map(t => [`#${t.id}`, t.titulo, t.estado]),
                    theme: 'grid',
                    headStyles: { fillColor: [150, 0, 0] }
                });
            }

            // Pie de página
            const pages = doc.internal.getNumberOfPages();
            for (let i = 1; i <= pages; i++) {
                doc.setPage(i);
                doc.setFontSize(8);
                doc.setTextColor(150, 150, 150);
                doc.text(`Página ${i} de ${pages}`, 10, 285);
            }

            doc.save(`Reporte_Sistema_${new Date().getTime()}.pdf`);
        } catch (error) {
            console.error("Error generating tabular report:", error);
            throw error;
        }
    }
}

export default new ReportService();
