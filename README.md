# Ticket Scanner Android app

The backend base URL is supplied to `BuildConfig` at build time. The default is
the PC Wi-Fi IPv4 address detected during setup; override it when that address
changes:

```powershell
$pcIpv4 = Get-NetIPAddress -AddressFamily IPv4 |
    Where-Object { $_.InterfaceAlias -like 'Wi-Fi*' -and $_.IPAddress -notmatch '^(127|169\.254)\.' } |
    Select-Object -First 1 -ExpandProperty IPAddress
.\gradlew.bat assembleDebug "-PBACKEND_BASE_URL=http://${pcIpv4}:1234/"
```

The app uses HTTP for local-network development. Keep the trailing slash in the
URL. The phone and PC must be on the same reachable network, and Windows
Firewall must allow inbound TCP port 1234.

Online ticket scans require a pre-provisioned `VALIDATOR` account. Public
registration creates customer accounts only. A signed-in `ADMIN` can use
Settings > Create account to register another administrator; the backend
authorizes this operation and assigns the role itself. The first administrator
must be provisioned through a trusted process. Access and refresh tokens are
stored in app-private preferences, excluded from backup, and attached to
protected requests; an expired access token is refreshed once. The offline
scanner continues to use its existing local ticket store.
