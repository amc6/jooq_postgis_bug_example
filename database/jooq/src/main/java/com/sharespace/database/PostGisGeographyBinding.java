package com.sharespace.database;

import org.jooq.Binding;
import org.jooq.BindingGetResultSetContext;
import org.jooq.BindingGetSQLInputContext;
import org.jooq.BindingGetStatementContext;
import org.jooq.BindingRegisterContext;
import org.jooq.BindingSQLContext;
import org.jooq.BindingSetSQLOutputContext;
import org.jooq.BindingSetStatementContext;
import org.jooq.Converter;
import org.jooq.impl.DSL;
import org.postgis.Geometry;
import org.postgis.PGgeometry;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * Implemented with the help of https://groups.google.com/forum/#!topic/jooq-user/TBQZCPTCvnk
 * and https://github.com/dmitry-zhuravlev/jooq-postgis-spatial/blob/master/src/main/kotlin/net/dmitry/jooq/postgis/spatial/binding/PostgisGeometryBinding.kt
 */

public class PostGisGeographyBinding implements Binding<Object, Geometry> {

    private static class GeographyConverter implements Converter<Object, Geometry> {

        @Override
        public Geometry from(Object t) {
            try {
                return t == null ? null : PGgeometry.geomFromString(t.toString());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Object to(Geometry g) {
            if (g == null) {
                return null;
            }

            return new PGgeometry(g);
        }

        @Override
        public Class<Object> fromType() {
            return Object.class;
        }

        @Override
        public Class<Geometry> toType() {
            return Geometry.class;
        }
    }

    private final GeographyConverter converter = new GeographyConverter();

    @Override
    public Converter<Object, Geometry> converter() {
        return converter;
    }

    @Override
    public void sql(BindingSQLContext<Geometry> ctx) throws SQLException {
        ctx.render().visit(DSL.sql("?::geography"));
    }

    @Override
    public void set(BindingSetStatementContext<Geometry> ctx) throws SQLException {
        ctx.statement().setObject(ctx.index(), ctx.convert(converter).value());
    }

    @Override
    public void get(BindingGetResultSetContext<Geometry> ctx) throws SQLException {
        ctx.convert(converter).value(ctx.resultSet().getObject(ctx.index()));
    }

    @Override
    public void get(BindingGetStatementContext<Geometry> ctx) throws SQLException {
        ctx.convert(converter).value(ctx.statement().getObject(ctx.index()));
    }

    @Override
    public void set(BindingSetSQLOutputContext<Geometry> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void get(BindingGetSQLInputContext<Geometry> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void register(BindingRegisterContext<Geometry> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}
