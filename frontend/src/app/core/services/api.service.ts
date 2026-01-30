import {BehaviorSubject, catchError, finalize, map, Observable, throwError} from 'rxjs';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {environment} from '../../../environments/environment';
import JSONBig from 'json-bigint';

/**
 * Central HTTP API service: GET/POST/PUT/DELETE with loading state and JSON-BigInt parsing.
 */
@Injectable({
  providedIn: 'root',
})
export class ApiService {
  private baseUrl = environment.apiEndpoint;
  private loadingSubject = new BehaviorSubject(false);
  /** Observable of global loading state. */
  public loading$ = this.loadingSubject.asObservable();

  constructor(private http: HttpClient){}

  /**
   * Performs a GET request; response is parsed with JSON-BigInt for large numbers.
   * @param endpoint - Path relative to API base (e.g. 'users/userlist')
   * @param options - Optional params and headers
   * @returns Observable of parsed response body
   */
  get<T>(endpoint: string, options?: { params?: any; headers?: any }): Observable<T> {
    this.loadingSubject.next(true);

    return this.http
      .get(`${this.baseUrl}/${endpoint}`, {
        ...options,
        responseType: 'text' as 'json'
      })
      .pipe(
        map((raw: any) => {
          return JSONBig({ storeAsString: true }).parse(raw as string) as T;
        }),
        catchError(this.handleError),
        finalize(() => this.loadingSubject.next(false))
      );
  }

  /**
   * Performs a POST request.
   * @param endpoint - Path relative to API base
   * @param data - Request body
   * @param options - Optional params and headers
   * @returns Observable of response body
   */
  post<T>(endpoint: string, data: any, options?: { params?: any; headers?: any }): Observable<T> {
    this.loadingSubject.next(true);
    return this.http.post<T>(`${this.baseUrl}/${endpoint}`, data, options).pipe(
      catchError(this.handleError),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  put<T>(endpoint: string, data: any, options?: { params?: any; headers?: any }): Observable<T> {
    this.loadingSubject.next(true);
    return this.http.put<T>(`${this.baseUrl}/${endpoint}`, data, options).pipe(
      catchError(this.handleError),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  /**
   * Performs a DELETE request.
   * @param endpoint - Path relative to API base
   * @param options - Optional params and headers
   * @returns Observable of response body
   */
  delete<T>(endpoint: string, options?: { params?: any; headers?: any }): Observable<T> {
    this.loadingSubject.next(true);
    return this.http.delete<T>(`${this.baseUrl}/${endpoint}`, options).pipe(
      catchError(this.handleError),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An unknown error occurred!';
    if(error.error instanceof ErrorEvent){
      errorMessage = `Client-side error: ${error.error.message}`;
    }else{
      errorMessage = `Server-side error: ${error.status} - ${error.message}`;
    }
    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
