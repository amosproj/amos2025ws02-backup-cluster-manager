import {BehaviorSubject, catchError, finalize, Observable, throwError} from 'rxjs';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  private baseUrl = environment.apiEndpoint;
  private loadingSubject = new BehaviorSubject(false);
  public loading$ = this.loadingSubject.asObservable();

  constructor(private http: HttpClient){}

  get<T>(endpoint: string, options?: { params?: any; headers?: any }): Observable<T> {
    this.loadingSubject.next(true);
    return this.http.get<T>(`${this.baseUrl}/${endpoint}`, options).pipe(
      catchError(this.handleError),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  post<T>(endpoint: string, data: any, options?: { params?: any; headers?: any }): Observable<T> {
    this.loadingSubject.next(true);
    return this.http.post<T>(`${this.baseUrl}/${endpoint}`, data, options).pipe(
      catchError(this.handleError),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  put<T>(endpoint: string, data: any):Observable<T> {
    this.loadingSubject.next(true);
    return this.http.put<T>(`${this.baseUrl}/${endpoint}`, data).pipe(
      catchError(this.handleError),
      finalize(()=>this.loadingSubject.next(false))
    )
  }
  
  delete<T>(endpoint: string):Observable<T> {
    this.loadingSubject.next(true);
    return this.http.delete<T>(`${this.baseUrl}/${endpoint}`).pipe(
      catchError(this.handleError),
      finalize(()=>this.loadingSubject.next(false))
    )
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
