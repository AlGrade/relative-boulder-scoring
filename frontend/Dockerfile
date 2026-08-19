# syntax=docker/dockerfile:1.7

# Same reasoning as the backend: the bundle Angular emits is platform-neutral, so this
# stage runs natively and only the nginx stage below is built for linux/amd64.
FROM --platform=$BUILDPLATFORM node:22-alpine AS build

WORKDIR /build

COPY package.json package-lock.json ./
RUN npm ci --ignore-scripts --no-audit --no-fund

COPY angular.json tsconfig*.json ./
COPY public/ ./public/
COPY src/ ./src/

RUN npm run build


# Serving the app and proxying /api/ to the backend. Port 8080 rather than 80 lets nginx
# run as an unprivileged user without NET_BIND_SERVICE.
FROM nginx:1.29-alpine AS static

COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=build --chown=nginx:nginx /build/dist/frontend/browser/ /usr/share/nginx/html/

USER nginx:nginx

EXPOSE 8080
STOPSIGNAL SIGQUIT

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget -q -O /dev/null http://127.0.0.1:8080/healthz || exit 1

# Skip the parent image's root-oriented config templating; the config and assets here
# are final.
ENTRYPOINT ["/usr/sbin/nginx"]
CMD ["-g", "daemon off;"]
