package com.cibertec.galcom.services.implementation;

import com.cibertec.galcom.entities.*;
import com.cibertec.galcom.repositories.*;
import com.cibertec.galcom.services.IReporteService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteService implements IReporteService {
    private final IReciboRepository reciboRepository;
    private final IEgresoRepository egresoRepository;
    private final IIngresoExternoRepository ingresoRepository;
    private final ISocioRepository socioRepository;
    private final IPuestoRepository puestoRepository;
    private final IBancoRepository bancoRepository;

    @Override
    @Transactional(readOnly = true)
    public byte[] generar(String tipo, LocalDate fecha, String mes) {
        String t = tipo == null ? "MOVIMIENTOS_DIARIOS" : tipo.toUpperCase();
        LocalDate dia = fecha != null ? fecha : LocalDate.now();
        YearMonth ym = mes != null && !mes.isBlank() ? YearMonth.parse(mes) : YearMonth.from(dia);
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle header = headerStyle(wb);
            switch (t) {
                case "MOVIMIENTOS_DIARIOS" -> movimientosDiarios(wb, header, dia);

                case "TOTALES" -> totalesMes(wb, header, ym);

                case "MENSUAL" -> resumenMensual(wb, header, ym);

                case "SOCIOS" -> socios(wb, header);

                case "NO_SOCIOS" -> noSocios(wb, header);

                case "EGRESOS" -> egresos(wb, header, ym);

                case "BANCOS" -> bancos(wb, header);

                default -> throw new IllegalArgumentException(
                        "Tipo de reporte no soportado"
                );
            }
            for (Sheet sheet : wb) autoSize(sheet);
            wb.write(out);
            return out.toByteArray();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el reporte", e);
        }
    }

    private void movimientosDiarios(Workbook wb, CellStyle h, LocalDate fecha) {
        Sheet s = wb.createSheet("Movimientos diarios");
        row(s,0,h,"Tipo","Correlativo / ID","Fecha","Responsable / Concepto","Monto","Estado");
        int r=1;
        for (ReciboEntity x : reciboRepository.findByFechaTipo(fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay(), null)) {
            String responsable = x.getSocio()!=null ? x.getSocio().getNombres()+" "+x.getSocio().getApellidos() : x.getPuesto()!=null ? "Puesto "+x.getPuesto().getNumero() : "General";
            row(s,r++,null,x.getTipo(),x.getNumeroCorrelativo(),x.getFecha().toString(),responsable,x.getMontoTotal(),x.getEstado());
        }
        for (EgresoEntity x : egresoRepository.buscar(fecha, fecha)) {
            row(s,r++,null,"EGRESO",String.valueOf(x.getId()),x.getFecha().toString(),x.getProveedor()+" - "+x.getMotivo(),x.getMontoTotal(),x.getEstado());
        }
    }

    private void totalesMes(
            Workbook wb,
            CellStyle h,
            YearMonth ym
    ) {

        LocalDate inicio = ym.atDay(1);
        LocalDate fin = ym.atEndOfMonth();

        List<ReciboEntity> recibos =
                reciboRepository.findByFechaTipo(
                        inicio.atStartOfDay(),
                        fin.plusDays(1).atStartOfDay(),
                        null
                );

        List<EgresoEntity> egresos =
                egresoRepository.buscar(inicio, fin);

        BigDecimal ingresos = recibos.stream()
                .filter(x ->
                        "EMITIDO".equals(x.getEstado())
                                && !"EGRESO".equals(x.getTipo())
                )
                .map(ReciboEntity::getMontoTotal)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        BigDecimal salidas = egresos.stream()
                .filter(x ->
                        "PROCESADO".equals(x.getEstado())
                )
                .map(EgresoEntity::getMontoTotal)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        long cantidadIngresos = recibos.stream()
                .filter(x ->
                        "EMITIDO".equals(x.getEstado())
                                && !"EGRESO".equals(x.getTipo())
                )
                .count();

        long cantidadEgresos = egresos.stream()
                .filter(x ->
                        "PROCESADO".equals(x.getEstado())
                )
                .count();

        Sheet s = wb.createSheet(
                "Totales " + ym
        );

        row(
                s,
                0,
                h,
                "Concepto",
                "Valor"
        );

        row(
                s,
                1,
                null,
                "Período",
                ym.toString()
        );

        row(
                s,
                2,
                null,
                "Cantidad de ingresos",
                cantidadIngresos
        );

        row(
                s,
                3,
                null,
                "Total ingresos",
                ingresos
        );

        row(
                s,
                4,
                null,
                "Cantidad de egresos procesados",
                cantidadEgresos
        );

        row(
                s,
                5,
                null,
                "Total egresos",
                salidas
        );

        row(
                s,
                6,
                null,
                "Saldo del período",
                ingresos.subtract(salidas)
        );
    }

    private void resumenMensual(Workbook wb, CellStyle h, YearMonth ym) {
        LocalDate ini=ym.atDay(1), fin=ym.atEndOfMonth();
        List<ReciboEntity> recibos=reciboRepository.findByFechaTipo(ini.atStartOfDay(),fin.plusDays(1).atStartOfDay(),null);
        List<EgresoEntity> egresos=egresoRepository.buscar(ini,fin);
        BigDecimal ingresos=recibos.stream().filter(x->"EMITIDO".equals(x.getEstado()) && !"EGRESO".equals(x.getTipo())).map(ReciboEntity::getMontoTotal).reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal salidas=egresos.stream().filter(x->"PROCESADO".equals(x.getEstado())).map(EgresoEntity::getMontoTotal).reduce(BigDecimal.ZERO,BigDecimal::add);
        Sheet s=wb.createSheet("Resumen "+ym);
        row(s,0,h,"Concepto","Monto");
        row(s,1,null,"Ingresos emitidos",ingresos);
        row(s,2,null,"Egresos procesados",salidas);
        row(s,3,null,"Saldo del período",ingresos.subtract(salidas));
        row(s,5,h,"Correlativo","Tipo","Fecha","Monto","Estado"); int r=6;
        for(ReciboEntity x:recibos) row(s,r++,null,x.getNumeroCorrelativo(),x.getTipo(),x.getFecha().toLocalDate().toString(),x.getMontoTotal(),x.getEstado());
    }

    private void socios(Workbook wb, CellStyle h) {
        Sheet s=wb.createSheet("Socios"); row(s,0,h,"Código","DNI","Nombres","Apellidos","Acción","Etapa","Estado"); int r=1;
        for(SocioEntity x:socioRepository.findAll()) row(s,r++,null,x.getCodigo(),x.getDni(),x.getNombres(),x.getApellidos(),x.getAccion(),x.getEtapa(),Boolean.TRUE.equals(x.getEstado())?"ACTIVO":"INACTIVO");
    }

    private void noSocios(Workbook wb, CellStyle h) {
        Sheet s=wb.createSheet("Puestos sin socio"); row(s,0,h,"Puesto","Ubicación","Inquilino","Documento","Estado"); int r=1;
        for(PuestoEntity x:puestoRepository.findAllConRelaciones()) if(x.getSocio()==null) row(s,r++,null,x.getNumero(),x.getUbicacion(),x.getInquilinoNombre(),x.getInquilinoDocumento(),x.getEstado());
    }

    private void egresos(Workbook wb, CellStyle h, YearMonth ym) {
        Sheet s=wb.createSheet("Egresos"); row(s,0,h,"ID","Fecha","Documento","Proveedor","Subtotal","Impuesto","Total","Motivo","Estado"); int r=1;
        for(EgresoEntity x:egresoRepository.buscar(ym.atDay(1),ym.atEndOfMonth())) row(s,r++,null,x.getId(),x.getFecha().toString(),x.getNumeroDocumento(),x.getProveedor(),x.getSubtotal(),x.getImpuesto(),x.getMontoTotal(),x.getMotivo(),x.getEstado());
    }

    private void bancos(Workbook wb, CellStyle h) {
        Sheet s=wb.createSheet("Bancos"); row(s,0,h,"Banco","Cuenta","CCI","Moneda","Tipo","Saldo inicial","Estado"); int r=1;
        for(BancoEntity x:bancoRepository.findAll()) row(s,r++,null,x.getNombre(),x.getNumeroCuenta(),x.getCci(),x.getMoneda(),x.getTipoCuenta(),x.getSaldoInicial(),Boolean.TRUE.equals(x.getEstado())?"ACTIVO":"INACTIVO");
    }

    private CellStyle headerStyle(Workbook wb){ CellStyle st=wb.createCellStyle(); Font f=wb.createFont(); f.setBold(true); st.setFont(f); return st; }
    private void row(Sheet s,int index,CellStyle style,Object... values){ Row r=s.createRow(index); for(int i=0;i<values.length;i++){ Cell c=r.createCell(i); Object v=values[i]; if(v instanceof Number n)c.setCellValue(n.doubleValue()); else c.setCellValue(v==null?"":String.valueOf(v)); if(style!=null)c.setCellStyle(style);} }
    private void autoSize(Sheet s){ int max=0; for(Row r:s)max=Math.max(max,r.getLastCellNum()); for(int i=0;i<max;i++){s.autoSizeColumn(i); if(s.getColumnWidth(i)>16000)s.setColumnWidth(i,16000);} }
}
