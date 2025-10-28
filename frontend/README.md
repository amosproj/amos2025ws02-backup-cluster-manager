# Frontend


## Example Project Structure
```bash
  src
├── app
│   ├── core                   # singleton services, interceptors, guards
│   │   ├── services
│   │   │   ├── api.service.ts
│   │   │   └── auth.service.ts
│   │   ├── interceptors
│   │   │   └── auth.interceptor.ts
│   │   └── guards
│   │       └── auth.guard.ts
│   │
│   ├── shared                 # reusable UI components, pipes, directives
│   │   ├── components
│   │   │   └── loader
│   │   └── pipes
│   │       └── format-bytes.pipe.ts
│   │
│   ├── features               # feature modules (each with routing, components, etc.)
│   │   ├── home
│   │   │   ├── home.component.ts
│   │   │   └── home.module.ts
│   │   ├── clusters
│   │   │   ├── clusters.component.ts
│   │   │   ├── clusters.service.ts
│   │   │   └── clusters.module.ts
│   │   └── nodes
│   │       ├── nodes.component.ts
│   │       ├── nodes.service.ts
│   │       └── nodes.module.ts
│   │
│   ├── app.component.ts
│   ├── app.module.ts
│   └── app-routing.module.ts
│
├── assets
├── environments
├── main.ts
├── index.html
└── styles.css
```


## Prerequisites
1. Install Nodejs: https://nodejs.org/en/download
2. Install Angular CLI globally using npm:
```bash
  npm install -g @angular/cli
```
3. Install all packages:
```bash
  npm install
```

## Development server

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Running unit tests

To execute unit tests with the [Karma](https://karma-runner.github.io) test runner, use the following command:

```bash
ng test
```

## Running end-to-end tests

For end-to-end (e2e) testing, run:

```bash
ng e2e
```

Angular CLI does not come with an end-to-end testing framework by default. You can choose one that suits your needs.

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.

## Angular CLI Documentation
This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 20.3.6.
